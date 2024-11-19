package tsetlin;

import com.github.kilianB.pcg.sync.PcgRR;
import org.apache.commons.lang3.ArrayUtils;
import util.QuickSort;

import java.util.ArrayList;
import java.util.HashMap;



public class FastUnitAutomaton {

	
	final int INT_SIZE = 32;
	
	private int nClauses;
	private int la_chunks;
	private int state_bits;
	private int clause_chunks;
	private int nFeatures;
	private int T;
	private int filter;
	private float max_specificity;
	private float s;
	private boolean boost;
	
	private int PREDICT;
	private int UPDATE;
	
	
	private int[][][] ta_state; //[CLAUSES][LA_CHUNKS][STATE_BITS];
	private int[] clause_output; //[CLAUSE_CHUNKS];
	private int[] feedback_to_la; //[LA_CHUNKS];
	private int[] feedback_to_clauses; //[CLAUSE_CHUNKS];
	private int[] clause_weights;
	
	PcgRR rng; 

	private boolean weighted;

	private int[] sorted_clause_index;
	private double class_probability;
	/**
	 * Maps a class from 0 to threshold-1 a popular vote of the clauses
	 */
	private HashMap<Integer, int[]> regression_clause_map;

	
	
	
	public FastUnitAutomaton(int threshold, int nFeatures, int nClauses, float max_specificity, boolean boost) {
		
		this.T = threshold;
		this.nFeatures = nFeatures;
		this.nClauses = nClauses;
		this.max_specificity = max_specificity;
		this.boost = boost;
		
		this.state_bits = 8;
		this.clause_chunks = (nClauses - 1)/INT_SIZE + 1;
		this.la_chunks = (2*nFeatures - 1)/INT_SIZE + 1;
		this.s = 2;
		
		PREDICT = 1;
		UPDATE = 0;
		rng = new PcgRR();
		
		if (((nFeatures*2) % 32) != 0) {
			this.filter  = (~(0xffffffff << ((nFeatures*2) % 32)));			
		} else {
			this.filter = 0xffffffff;
		}
		weighted = true;
		sorted_clause_index = null;
	}
	
	public FastUnitAutomaton initialize() {
		
		ta_state = new int[nClauses][la_chunks][state_bits];
		clause_output = new int[clause_chunks];
		feedback_to_la = new int[la_chunks];
		feedback_to_clauses = new int[clause_chunks];
		clause_weights = new int[nClauses];
		sorted_clause_index = null;
		
		
		for (int j = 0; j < nClauses; ++j) {
			for (int k = 0; k < la_chunks; ++k) {
				for (int b = 0; b < state_bits-1; ++b) {											
					ta_state[j][k][b] = ~0;
				}
				ta_state[j][k][state_bits-1] = 0;
			}
			clause_weights[j] = 1;
		}
		
		return this;
	}
	
	/**
	 * Initializes a regression clause mapping
	 * Maps a regression value to a vote on clauses
	 */
	public void initialize_interpretable_regression_map()  {
		
		System.out.println("Initializing Regression Map");
		
		regression_clause_map = new HashMap<Integer, int[]>();	
		for(int i = 0; i < T; i++) {
			regression_clause_map.put(i, new int[nClauses]);
		}	
	}
	

    public void initialize_random_streams(int clause) {
		
		// Initialize all bits to zero	
		feedback_to_la = new int[la_chunks];

		int n = 2 * nFeatures;
		double p = 1f / s;
				
		if(max_specificity > 0) {
			p = 1.0 / (s + 1.0 * clause * (max_specificity - s) / nClauses);
		}
		int active = (int)((n * p * (1 - p))*rng.nextGaussian() + (n * p));
		
		active = active >= n ? n : active;
		active = active < 0 ? 0 : active;
		
		while (active-- != 0) {
			
			long rand = Integer.toUnsignedLong(~rng.nextInt());
			int f = (int)(rand % n);

			while ((feedback_to_la[f / 32] & (1 << (f % 32))) != 0) {
				f = (int) (Integer.toUnsignedLong(~rng.nextInt()) % n);
		    }
			feedback_to_la[f / 32] |= 1 << (f % 32);
		}
		
		
	}
    
    
 // Increment the states of each of those 32 Tsetlin Automata flagged in the active bit vector.
    private void inc(int clause, int chunk, int active) {
    	
    	int carry, carry_next;
    	carry = active;
    	for (int b = 0; b < state_bits; ++b) {
    		if (carry == 0)
    			break;

    		carry_next = ta_state[clause][chunk][b] & carry; // Sets carry bits (overflow) passing on to next bit
    		ta_state[clause][chunk][b] = ta_state[clause][chunk][b] ^ carry; // Performs increments with XOR
    		carry = carry_next;
    	}

    	if (carry > 0) {
    		for (int b = 0; b < state_bits; ++b) {
    			ta_state[clause][chunk][b] |= carry;
    		}
    	} 	
    }

    // Decrement the states of each of those 32 Tsetlin Automata flagged in the active bit vector.
    private void dec(int clause, int chunk, int active) {
    	
    	int carry, carry_next;

    	carry = active;
    	for (int b = 0; b < state_bits; ++b) {
    		if (carry == 0)
    			break;

    		carry_next = (~ta_state[clause][chunk][b]) & carry; // Sets carry bits (overflow) passing on to next bit
    		ta_state[clause][chunk][b] = ta_state[clause][chunk][b] ^ carry; // Performs increments with XOR
    		carry = carry_next;
    	}

    	if (carry > 0) {
    		for (int b = 0; b < state_bits; ++b) {
    			ta_state[clause][chunk][b] &= ~carry;
    		}
    	} 
    }
    
    
	/* Sum up the votes for each class */
	public int sum_up_class_votes() {
		
		int class_sum = 0;

		int pos_sum_weights = 0;
		int positive_polarity_votes = 0;
		int negative_polarity_votes = 0;
		int neg_sum_weights = 0;
		
		for (int j = 0; j < nClauses; j++) {
			
			int clause_chunk = j / 32;
			int clause_pos = j % 32;

			if (j % 2 == 0) {
				class_sum += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1 : 0);
				
				positive_polarity_votes += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1 : 0);
				pos_sum_weights += clause_weights[j];
			} else {
				class_sum -= clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1 : 0);
				
				negative_polarity_votes += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1 : 0);
				neg_sum_weights += clause_weights[j];
			}
		}

		class_sum = (class_sum > T) ? T : class_sum;
		class_sum = (class_sum < -T) ? -T : class_sum;

		double pos_polar = 1.0*positive_polarity_votes/(1.0*pos_sum_weights);
		double neg_polar = 1.0*negative_polarity_votes/(1.0*neg_sum_weights);
		
		double prob = Math.min(1.0, .50 + pos_polar - neg_polar);
		
		setClass_probability(prob);
				
		return class_sum;
	}	
	
    
    
    /* Calculate the output of each clause using the actions of each Tsetline Automaton. */
    private void calculate_clause_output(int Xi[], int predict) {
    	
    	clause_output = new int[clause_chunks];
    	sorted_clause_index = null;

    	for (int j = 0; j < nClauses; j++) {
    		
    		int output = 1;
    		int all_exclude = 1;
    		
    		for (int k = 0; k < la_chunks-1; k++) {
    			output = (output == 1) && ((ta_state[j][k][state_bits-1] & Xi[k]) == ta_state[j][k][state_bits-1]) ? 1 : 0;
	
    			//System.out.println(k + " " + output + " " + Integer.toUnsignedLong(ta_state[j][k][state_bits-1]) + "  " + Integer.toUnsignedLong(Xi[k]));
    			
    			if (output == 0) {
    				break;
    			}
    			all_exclude = (all_exclude == 1) && (ta_state[j][k][state_bits-1] == 0) ? 1 : 0;
    			
    			
    		}

    		output = (output == 1) && ((ta_state[j][la_chunks-1][state_bits-1] & Xi[la_chunks-1] & filter) ==
    			(ta_state[j][la_chunks-1][state_bits-1] & filter)) ? 1 : 0;

    		//System.out.println(output + " " + Integer.toUnsignedLong(ta_state[j][la_chunks-1][state_bits-1]) + "  " + Integer.toUnsignedLong(Xi[la_chunks-1]) + " " + filter);
    		
    		
    		all_exclude = (all_exclude == 1) && ((ta_state[j][la_chunks-1][state_bits-1] & filter) == 0) ? 1 : 0;
    		//all_exclude = (all_exclude == 1) && (ta_state[j][la_chunks-1][state_bits-1] == 0) ? 1 : 0;

    		output = (output == 1) && !(predict == PREDICT && all_exclude == 1) ? 1 : 0;
    	
    		
    		if (output == 1) {
    			int clause_chunk = j / 32;
    			int clause_chunk_pos = j % 32;

     			clause_output[clause_chunk] |= (1 << clause_chunk_pos);
     		}

     	}
    }
    
    
	/**************************************/
	/*** The Regression Tsetlin Machine ***/
	/**************************************/

	/* Sum up the votes for each class */
	public int sum_up_class_votes_regression() {
		
		int class_sum = 0;

		for (int j = 0; j < nClauses; j++) {
			
			int clause_chunk = j / 32;
			int clause_pos = j % 32;

			//System.out.println(j + " " + clause_chunk + " " + Integer.toBinaryString(clause_output[clause_chunk]) + " " + Integer.toBinaryString((1 << clause_pos)));
			
		    class_sum += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1:0);		
			
			//class_sum += ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1:0);
			
			//System.out.println("Outcome: " + Integer.toBinaryString(clause_output[clause_chunk]) + "  " + Integer.toBinaryString((clause_output[clause_chunk] & (1 << clause_pos))));
		}
		//System.out.println("Class sum: " + class_sum);
		class_sum = (class_sum > T) ? T : class_sum;

		return class_sum;
	}
	
	
	/* Sum up the votes for each class */
	public void sum_up_class_votes_regression_account() {
		
		ArrayList<Integer> class_list = new ArrayList<Integer>();
		int class_sum = 0;

		
		for (int j = 0; j < nClauses; j++) {
			
			int clause_chunk = j / 32;
			int clause_pos = j % 32;
			
		    class_sum += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1:0);		
			
		    if((clause_output[clause_chunk] & (1 << clause_pos)) != 0) {
		        class_list.add(j);
		    }
		}

		class_sum = (class_sum >= T) ? (T-1) : class_sum;
		for(int i = 0; i < class_list.size(); i++) {
			regression_clause_map.get(class_sum)[class_list.get(i)]++;
		}
	}
	
	
	
	
	// The Tsetlin Machine can be trained incrementally, one training example at a time.
	// Use this method directly for online and incremental training.

	public int update_regression(int[] X, int target) {
		
		int[] Xi = bit_encode(X);
		
		/*******************************/
		/*** Calculate Clause Output ***/
		/*******************************/
		
//		System.out.println("Size: " + Xi.length);
//		for(int i = 0; i < Xi.length; i++) {
//			System.out.print(Integer.toBinaryString(Xi[i]) + " ");
//		}
//		System.out.println("");
		
		calculate_clause_output(Xi, UPDATE);

		/***************************/
		/*** Sum up Clause Votes ***/
		/***************************/

		int class_sum = sum_up_class_votes_regression();

		/*********************************/
		/*** Train Individual Automata ***/
		/*********************************/
		
		// Calculate feedback to clauses

		int prediction_error = class_sum - target; 
 
		//System.out.println("class sum " + class_sum + " " + target + " prediction error: " + Math.pow(1.0*prediction_error/T, 2) + " " + Math.abs(1.0*prediction_error/T));
		
		for (int j = 0; j < clause_chunks; j++) {
		 	feedback_to_clauses[j] = 0;
		}

		for (int j = 0; j < nClauses; j++) {
			int clause_chunk = j / 32;
			int clause_chunk_pos = j % 32;

		 	feedback_to_clauses[clause_chunk] |= (rng.nextFloat() <= Math.pow(1.0*prediction_error/T, 2) ? 1:0) << clause_chunk_pos;
		}
			

		for (int j = 0; j < nClauses; j++) {
			int clause_chunk = j / 32;
			int clause_chunk_pos = j % 32;
			
			if ( ((feedback_to_clauses[clause_chunk] & (1 << clause_chunk_pos) ) == 0) ) {
				continue;
			}
			
			if (prediction_error > 0) {
				if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
					// Type II Feedback
					
					if (weighted && clause_weights[j] > 1) {clause_weights[j]--;}

					for (int k = 0; k < la_chunks; ++k) {
    					inc(j, k, (~Xi[k]) & (~ta_state[j][k][state_bits-1]));
    				}
					
				}
			} else if (prediction_error < 0) {
				// Type I Feedback

				initialize_random_streams(j);

				if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
	    				
					    if (weighted) clause_weights[j]++;
    
	    				for (int k = 0; k < la_chunks; ++k) {
	    					if(boost)
	    		 				inc(j, k, Xi[k]);
	    					else
	    						inc(j, k, Xi[k] & (~feedback_to_la[k]));
	    					
	    		 			dec(j, k, (~Xi[k]) & feedback_to_la[k]);
	    				}
	    		} 
	    		else {
	    				for (int k = 0; k < la_chunks; ++k) {
	    					dec(j, k, feedback_to_la[k]);
	    				}
	    		}
			}				
		}
		return class_sum;
	}
	
	
	public int score_regression(int[] X) {
		
		int[] Xi = bit_encode(X);
		
		/*******************************/
		/*** Calculate Clause Output ***/
		/*******************************/

		calculate_clause_output(Xi, PREDICT);

		/***************************/
		/*** Sum up Clause Votes ***/
		/***************************/

		return sum_up_class_votes_regression();
	}    
    
	/**
	 * Evaluate an input while mapping the clauses
	 * @param Xi
	 * @return
	 */
	public void score_regression_range(int[] X) {

		int[] Xi = bit_encode(X);
		
		calculate_clause_output(Xi, PREDICT);
		sum_up_class_votes_regression_account();
	}
	
	public void learn_regression_range(int[][] Xi) {
		
		for(int i = 0; i < Xi.length; i++) {
			score_regression_range(Xi[i]);
		}		
	}
	
	
    
    int get_state(int clause, int la) {
    	
    	int la_chunk = la / INT_SIZE;
    	int chunk_pos = la % INT_SIZE;

    	int state = 0;
    	for (int b = 0; b < state_bits; ++b) {
    		if ((ta_state[clause][la_chunk][b] & (1 << chunk_pos)) != 0 ) {
    			state |= 1 << b; 
    		}
    	}

    	return state;
    }

    int tm_action(int clause, int la) {
    	
    	int la_chunk = la / INT_SIZE;
    	int chunk_pos = la % INT_SIZE;

    	return (ta_state[clause][la_chunk][state_bits-1] & (1 << chunk_pos)) != 0 ? 1 : 0;
    }
    
    
    int[][][] getState() {
    	return ta_state;
    }
    
    /******************************************/
    /*** Online Training of Tsetlin Machine ***/
    /******************************************/

    // The Tsetlin Machine can be trained incrementally, one training example at a time.
    // Use this method directly for online and incremental training.

    public void update(int Xi[], int target) {
    	
    	
    	/*******************************/
    	/*** Calculate Clause Output ***/
    	/*******************************/

    	
    	calculate_clause_output(Xi, UPDATE);

    	/***************************/
    	/*** Sum up Clause Votes ***/
    	/***************************/

    	int class_sum = sum_up_class_votes();

    	
    	
    	//System.out.println("Class sum: " + class_sum);
    	/*********************************/
    	/*** Train Individual Automata ***/
    	/*********************************/
    	
    	// Calculate feedback to clauses

    	float p = (1f/(T*2))*(T + (1 - 2*target)*class_sum);
    	feedback_to_clauses = new int[clause_chunks];
    	
    	//System.out.println("Class sum: " + class_sum + " p: " + p);
    	
      	for (int j = 0; j < nClauses; j++) {
      		
        	int clause_chunk = j / INT_SIZE;
            int clause_chunk_pos = j % INT_SIZE;

            feedback_to_clauses[clause_chunk] |= (rng.nextFloat() <= p ? 1 : 0) << clause_chunk_pos;
        }

    	for (int j = 0; j < nClauses; j++) {
    		
    		int clause_chunk = j / INT_SIZE;
    		int clause_chunk_pos = j % INT_SIZE;
	
    		if ( ((feedback_to_clauses[clause_chunk] & (1 << clause_chunk_pos) ) == 0) ) {
				continue;
			}
    		
  
    		if ((2*target-1) * (1 - 2 * (j & 1)) == -1) {
    			if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
    				// Type II Feedback

					if (weighted && clause_weights[j] > 1) {
						clause_weights[j]--;
					}
    				
    				for (int k = 0; k < la_chunks; ++k) {
    					inc(j, k, (~Xi[k]) & (~ta_state[j][k][state_bits-1]));
    				}
    			}
    		} 
    		else if ((2*target-1) * (1 - 2 * (j & 1)) == 1) {
    			// Type I Feedback

    			initialize_random_streams(j);

    			if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
    				
    				if(weighted) clause_weights[j]++;
    				
    				for (int k = 0; k < la_chunks; ++k) {
    						
    					if(boost)
    		 				inc(j, k, Xi[k]);
    					else
    						inc(j, k, Xi[k] & (~feedback_to_la[k]));
    					
    		 			dec(j, k, (~Xi[k]) & feedback_to_la[k]);
    				}
    			} 
    			else {
    				for (int k = 0; k < la_chunks; ++k) {
    					dec(j, k, feedback_to_la[k]);
    				}
    			}
    		}
    	}
    }
    
    
	public int score(int Xi[]) {
				
		/*******************************/
		/*** Calculate Clause Output ***/
		/*******************************/

		calculate_clause_output(Xi, PREDICT);

		/***************************/
		/*** Sum up Clause Votes ***/
		/***************************/

		return sum_up_class_votes();
	}


	
	public int[] interpretablePrediction(int[] Xi, int max_class) {
				
		int j, k;
		int action_include, action_include_negated;
	
		int[] local_feature_strength = new int[2*nFeatures + 1];
		
		
		for (j = 0; j < nClauses; j++) {
		  
			int clause_chunk = j / 32;
			int clause_pos = j % 32;
			
			if( ((clause_output[clause_chunk] & (1 << clause_pos)) != 0) && j%2 == 0) {	
				
				for (k = 0; k < nFeatures; k++) {
					
			    	int la_chunk = k / INT_SIZE;
			    	int chunk_pos = k % INT_SIZE;
					
					action_include = tm_action(j, k);
					action_include_negated = tm_action(j, nFeatures + k);
					
					if(action_include == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) != 0)) {
						local_feature_strength[k]++;
					}
					
					if(action_include_negated == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) == 0) ) {
						local_feature_strength[nFeatures + k]++;
					}
				}	
			}
		}
		
		local_feature_strength[local_feature_strength.length - 1] = max_class;
		return local_feature_strength;		
	}
	
	
	/**
	 * Gives the strength of the features from the clauses 
	 * given positive polarity, namely even 
	 * indexed clauses j % 2 = 0 
	 * Should only be used for multiclass classification 
	 * and only on the targeted risky class (eg. failure, test positive, etc)
	 * otherwise, hard to interpret
	 * @param Xi
	 * @return
	 */
	public int[] riskPredictionPositivePolarity(int[] Xi) {
	
		
		int j, k;
		int action_include, action_include_negated;
	
		int[] risk_strength = new int[2*nFeatures];
		
		
		for (j = 0; j < nClauses; j++) {
		  
			int clause_chunk = j / 32;
			int clause_pos = j % 32;
			
			if( ((clause_output[clause_chunk] & (1 << clause_pos)) != 0) && j % 2 == 0 ) {	
				
				for (k = 0; k < nFeatures; k++) {
					
			    	int la_chunk = k / INT_SIZE;
			    	int chunk_pos = k % INT_SIZE;
					
					action_include = tm_action(j, k);
					action_include_negated = tm_action(j, nFeatures + k);
					
					if(action_include == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) != 0)) {
						risk_strength[k]++;
					}
					
					if(action_include_negated == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) == 0) ) {
						risk_strength[nFeatures + k]++;
					}
				}	
			}
		}		
		return risk_strength;		
	}
	
	
	/**
	 * Gives the strength of the features from the clauses 
	 * given negative polarity, namely even 
	 * indexed clauses j % 2 = 1
	 * Should be used on the non-risk class  
	 * @param Xi
	 * @return
	 */
	public int[] riskPredictionNegativePolarity(int[] Xi) {
		
		int j, k;
		int action_include, action_include_negated;
	
		int[] risk_strength = new int[2*nFeatures];
		
		
		for (j = 0; j < nClauses; j++) {
		  
			int clause_chunk = j / 32;
			int clause_pos = j % 32;
			
			if( ((clause_output[clause_chunk] & (1 << clause_pos)) != 0) && j % 2 != 0 ) {	
				
				for (k = 0; k < nFeatures; k++) {
					
			    	int la_chunk = k / INT_SIZE;
			    	int chunk_pos = k % INT_SIZE;
					
					action_include = tm_action(j, k);
					action_include_negated = tm_action(j, nFeatures + k);
					
					if(action_include == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) != 0)) {
						risk_strength[k]++;
					}
					
					if(action_include_negated == 1 && ((Xi[la_chunk] & (1 << chunk_pos)) == 0) ) {
						risk_strength[nFeatures + k]++;
					}
				}	
			}
		}		
		return risk_strength;		
	}
	
	
	
	/**
	 * Explainabiltiy interface goes here
	 * 
	 * The idea is to have an overview of the accountability of each 
	 * input feature that either supports a positive output from a clause
	 * or its negation supports a positive output of a clause
	 * 
	 * If a feature is included in a clause more often than it is negated, and 
	 * the collection of clauses has positive output, then the idea 
	 * is that the highly prominent features were positive reinforcement in its
	 * decision
	 * 
	 * @return int array of length 2*nFeatures where 0 < n < nFeatures are the positive 
	 * identifying traits and the nFeatures < n < 2*nFeatures are the negative
	 */
	public int[] computeFeatureStrength() {
		
		int[] feature_strength = new int[2*nFeatures];
		
		for(int k = 0; k < nFeatures; k++) {
			for(int j = 0; j < nClauses; j++) {
			
				feature_strength[k] += tm_action(j, k);
				feature_strength[nFeatures + k] += tm_action(j, nFeatures + k);
			}	
		}		
		return feature_strength;
	}
	
	public int[] computeWeightedFeatureStrength() {
		
		int[] feature_strength = new int[2*nFeatures];
		
		for(int k = 0; k < nFeatures; k++) {
			for(int j = 0; j < nClauses; j++) {			
				if(j % 2 == 0) {
					feature_strength[k] += tm_action(j, k)*clause_weights[j];
					feature_strength[nFeatures + k] += tm_action(j, nFeatures +k)*clause_weights[j];
				}		
			}	
		}		
		return feature_strength;
	}
	

	
	/**
	 * Computes the feature strength of the top clause_weights
	 * by importance
	 * @param top
	 * @return
	 */
	public int[] computeWeightedFeatureStrength(int top) {
		
		if(sorted_clause_index == null) {
			sortClauseWeights();
		}
		
		int[] feature_strength = new int[2*nFeatures];
		
		for(int k = 0; k < nFeatures; k++) {
			for(int j = 0; j < top; j++) {			
				if(j % 2 == 0) {
					feature_strength[k] += tm_action(sorted_clause_index[j], k)*clause_weights[sorted_clause_index[j]];
					feature_strength[nFeatures + k] += tm_action(sorted_clause_index[j], nFeatures +k)*clause_weights[sorted_clause_index[j]];
				}
			}	
		}		
		return feature_strength;
	}
	
	public int[] computeFeatureClauseImportance(int top, int[] target_clauses) {
		
		int[] feature_strength = new int[2*nFeatures];
		int[] sorted = sortClauses(target_clauses);
		
		for(int i = 0; i < target_clauses.length; i++) {
			System.out.print(sorted[i] + " ");
		}
		System.out.println("");
		
	
		for(int j = 0; j < 10; j++) {
			for(int k = 0; k < nFeatures; k++) {
				
				feature_strength[k] += tm_action(sorted[j], k);
				feature_strength[nFeatures + k] += tm_action(sorted[j], nFeatures + k);
			}	
		}	
		return feature_strength;		
	}
	
	
	
	public void sortClauseWeights() {
		


		sorted_clause_index = new int[clause_weights.length];	
		for(int j = 0; j < clause_weights.length; j++) {
			sorted_clause_index[j] = j;
        }
		
		int[] copy_clause_weights = ArrayUtils.clone(clause_weights);
		QuickSort.sort(copy_clause_weights, sorted_clause_index);
        ArrayUtils.reverse(sorted_clause_index);
		
        for(int j = 0; j < clause_weights.length; j++) {
        	System.out.println(j + " " + sorted_clause_index[j] + " " + clause_weights[sorted_clause_index[j]] 
        			+ " " + clause_weights[j] + " " + copy_clause_weights[j]);
        } 
	}
	
	public static int[] sortClauses(int[] target_clauses) {
		
		System.out.println("Sorting..");

		int[] sorted_index = new int[target_clauses.length];	
		for(int j = 0; j < target_clauses.length; j++) {
			sorted_index[j] = j;
        }
		
		int[] copy_target_clauses = ArrayUtils.clone(target_clauses);
		QuickSort.sort(copy_target_clauses, sorted_index);
        ArrayUtils.reverse(sorted_index);	
        return sorted_index;
	}
	
	
	
	public int[] getGlobalFeatureImportance() {
		return computeWeightedFeatureStrength();
	}
	
	
	/**
	 * Returns the weights of the clauses for this class 
	 *
	 * @return
	 */
	public int[] getClauseStrength() {
		return clause_weights;
	}
	
	public int getClauseOutput(int j) {
		return clause_output[j];	
	}
	
	public int[] getClauseOutput() {
		return clause_output;	
	}
	
	public int getNClauses() {
		return nClauses;
	}
	
	public int[][] bit_encoder(int[][] X) {
		
		
		int FEATURES = X[0].length;
		int chunk_size = (2*FEATURES - 1)/INT_SIZE + 1;
		int num_samples = X.length;
		
		int[][] X_encoded = new int[num_samples][chunk_size];
		
		for (int i = 0; i < num_samples; i++) {
			

			
//			for(int k = 0; k < record.length; k++) {
//				System.out.print(record[k] + " ");
//			}
//			System.out.println("");
			
			for (int j = 0; j < FEATURES; j++) {
				if (X[i][j] == 1) {
					int chunk_nr = j / INT_SIZE ;
					int chunk_pos = j % INT_SIZE;
					X_encoded[i][chunk_nr] |= (1 << chunk_pos);
				} else {
					int chunk_nr = (j + FEATURES) / INT_SIZE;
					int chunk_pos = (j + FEATURES) % INT_SIZE;
					X_encoded[i][chunk_nr] |= (1 << chunk_pos);
				}
			}

		}
				
		return X_encoded;
	}
	
	public int[] bit_encode(int[] X) {
		
		int[] X_encoded = new int[la_chunks];
			for (int j = 0; j < nFeatures; j++) {
				if (X[j] == 1) {
					int chunk_nr = j / 32 ;
					int chunk_pos = j % 32;
					X_encoded[chunk_nr] |= (1 << chunk_pos);
				} else {
					int chunk_nr = (j + nFeatures) / 32;
					int chunk_pos = (j + nFeatures) % 32;
					X_encoded[chunk_nr] |= (1 << chunk_pos);
				}
			}
		return X_encoded;
	}
	
	public int[] predict_interpret(int[] xi) {
		
		int val = score_regression(xi);
		return interpretablePrediction(xi, val);
	}
	
	
	
	public void setMaxLearnRate(float learn_rate) {
		this.max_specificity = learn_rate;
		
	}
	
	public void setY_min(float target_min) {
		
	}

	public void setY_max(float target_max) {
	}

	public void reset_clause_output() {
		for(int i = 0; i < clause_output.length; i++) {
			clause_output[i] = ~0;
		}
	}
	
	/**
	 * Returns the distribution of most popular clauses
	 * @param target
	 * @return
	 */
	public int[] getClauseMapRegression(int target) {
		return regression_clause_map.get(target);
	}
	

	

	public int getThreshold() {
		// TODO Auto-generated method stub
		return T;
	}

	public int[] getFeedback() {
		
		return feedback_to_clauses;
	}

	public int getState_bits() {
		// TODO Auto-generated method stub
		return state_bits;
	}

	public double getClass_probability() {
		return class_probability;
	}

	public void setClass_probability(double class_probability) {
		this.class_probability = class_probability;
	}



	
	

}