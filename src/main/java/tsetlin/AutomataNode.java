package tsetlin;

import com.github.kilianB.pcg.sync.PcgRR;
import org.apache.commons.lang3.ArrayUtils;
import util.QuickSort;

import java.util.ArrayList;



/**
 * A generalized computational node for learning a binary input with a group of clauses
 * 
 * Features:
 *  Generalized convolutional structure for up to 3-dimensional learning
 *  Drop-clause for regularization of clause structure
 *  Integer weighted clauses for faster learning and increased interpretability
 *   
 * @author lisztian
 *
 */
public class AutomataNode {

	
	final int INT_SIZE = 32;
	
	private ConvolutionEncoder encoder;
	private int number_of_patches;
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
	private int[] output_one_patches;
	private int[] clause_patch;
	private float[] clause_patch_coverage;
	private int[] drop_clause;
	
	
	PcgRR rng; 

	private boolean weighted;
	private float clause_drop_p = 0;

	private int[] clause_index;
	private double class_probability;

	private ArrayList<Integer> regression_clauses;


	private int[][] clause_feature_strength;

	/**
	 * Instantiates a general convolutional automata machine
	 * with an encoder, threshold, and other parameters
	 * @param encoder
	 * @param threshold
	 * @param nClauses
	 * @param max_specificity
	 * @param boost
	 */
	public AutomataNode(ConvolutionEncoder encoder, int threshold, int nClauses, float max_specificity, boolean boost, float clause_drop_p) {
		
		this.encoder = encoder;
		this.number_of_patches = encoder.getNumber_of_patches();
		this.nFeatures = encoder.getNumber_of_features();
		this.T = threshold;
		
		this.clause_drop_p = clause_drop_p;
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
		
		
		
	}
	
	public AutomataNode initialize() {
		
		ta_state = new int[nClauses][la_chunks][state_bits];
		clause_output = new int[clause_chunks];
		drop_clause = new int[clause_chunks];
		feedback_to_la = new int[la_chunks];
		feedback_to_clauses = new int[clause_chunks];
		clause_weights = new int[nClauses];
		clause_feature_strength = new int[nClauses][2*nFeatures];
		
		output_one_patches = new int[number_of_patches];
		clause_patch = new int[nClauses];
		clause_patch_coverage = new float[nClauses];
		
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
	
	
	public void updateDropClause() {
		
		drop_clause = new int[clause_chunks];
		
		if(clause_drop_p > 0) {
			
			for(int j = 0; j < nClauses; j++) {
				
				int clause_chunk = j / 32;
				int clause_chunk_pos = j % 32;
				
				if(rng.nextFloat() < clause_drop_p) {
					drop_clause[clause_chunk]  |= (1 << clause_chunk_pos);
				}	
			}		
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
    public void calculate_clause_output(int Xi[], int predict) {
    	
    	int output_one_patches_count = 0;
    	clause_output = new int[clause_chunks];

    	for (int j = 0; j < nClauses; j++) {
    		
			int clause_chunk = j / 32;
			int clause_chunk_pos = j % 32;
    		
    		if(predict == UPDATE && (drop_clause[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
    			continue;
    		}
    		
    		output_one_patches_count = 0;
    		clause_patch_coverage[j] = 0f;
    		
    		for(int patch = 0; patch < number_of_patches; ++patch) {
	    		
    			int output = 1;
	    		int all_exclude = 1;
	    		
	    		for (int k = 0; k < la_chunks-1; k++) {
	    			output = (output == 1) && ((ta_state[j][k][state_bits-1] & Xi[patch*la_chunks + k]) == ta_state[j][k][state_bits-1]) ? 1 : 0;
		
	    			if (output == 0) {
	    				break;
	    			}
	    			all_exclude = (all_exclude == 1) && (ta_state[j][k][state_bits-1] == 0) ? 1 : 0;
	    		    			
	    		}
	
	    		output = (output == 1) && ((ta_state[j][la_chunks-1][state_bits-1] & Xi[patch*la_chunks + la_chunks-1] & filter) ==
	    			(ta_state[j][la_chunks-1][state_bits-1] & filter)) ? 1 : 0;
		    		
	    		all_exclude = (all_exclude == 1) && ((ta_state[j][la_chunks-1][state_bits-1] & filter) == 0) ? 1 : 0;

	    		output = (output == 1) && !(predict == PREDICT && all_exclude == 1) ? 1 : 0;
	    	
	    		
	    		if (output == 1) {
	    			output_one_patches[output_one_patches_count] = patch;
					output_one_patches_count++;
	     		}
    		}
    		
    		if (output_one_patches_count > 0) {
    			
     			clause_output[clause_chunk] |= (1 << clause_chunk_pos);

     			int patch_id = (int) (Integer.toUnsignedLong(~rng.nextInt()) % output_one_patches_count);
    	 		clause_patch[j] = output_one_patches[patch_id];  	 		
    	 		clause_patch_coverage[j] = 1f*output_one_patches_count/number_of_patches;
     		}
    		
     	}
    }
    
    
	/**************************************/
	/*** The Regression Tsetlin Machine ***/
	/**************************************/

	/* Sum up the votes for each class 
	 * 
	 * Counts which clauses are supporting positive reinforcement
	 * 
	 * */
	public int sum_up_class_votes_regression() {
		
		int class_sum = 0;

		regression_clauses = new ArrayList<Integer>();
		for (int j = 0; j < nClauses; j++) {
			
			int clause_chunk = j / 32;
			int clause_pos = j % 32;

			//System.out.println(j + " " + clause_chunk + " " + Integer.toBinaryString(clause_output[clause_chunk]) + " " + Integer.toBinaryString((1 << clause_pos)));
			
		    class_sum += clause_weights[j] * ((clause_output[clause_chunk] & (1 << clause_pos)) != 0 ? 1:0);		
			
		    regression_clauses.add(j);
			
		}
		//System.out.println("Class sum: " + class_sum);
		class_sum = (class_sum > T) ? T : class_sum;
		//System.out.println("Class sum: " + class_sum);
		return class_sum;
	}
	
	
	// The Tsetlin Machine can be trained incrementally, one training example at a time.
	// Use this method directly for online and incremental training.

	public int update_regression(int[] Xi, int target) {
		
		/*******************************/
		/*** Calculate Clause Output ***/
		/*******************************/
					
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
		 	//feedback_to_clauses[clause_chunk] |= (rng.nextFloat() <= Math.abs(1.0*prediction_error/T) ? 1:0) << clause_chunk_pos;
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
    					inc(j, k, (~Xi[clause_patch[j]*la_chunks + k]) & (~ta_state[j][k][state_bits-1]));
    				}
					
				}
			} else if (prediction_error < 0) {
				// Type I Feedback

				initialize_random_streams(j);

				if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
	    				
					    if (weighted) clause_weights[j]++;
    
	    				for (int k = 0; k < la_chunks; ++k) {
	    					if(boost)
	    		 				inc(j, k, Xi[clause_patch[j]*la_chunks + k]);
	    					else
	    						inc(j, k, Xi[clause_patch[j]*la_chunks + k] & (~feedback_to_la[k]));
	    					
	    		 			dec(j, k, (~Xi[clause_patch[j]*la_chunks + k]) & feedback_to_la[k]);
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
	
	
	public int score_regression(int[] Xi) {
		/*******************************/
		/*** Calculate Clause Output ***/
		/*******************************/

		calculate_clause_output(Xi, PREDICT);

		/***************************/
		/*** Sum up Clause Votes ***/
		/***************************/

		return sum_up_class_votes_regression();
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

    public int tm_action(int clause, int la) {
    	
    	int la_chunk = la / INT_SIZE;
    	int chunk_pos = la % INT_SIZE;

    	return (ta_state[clause][la_chunk][state_bits-1] & (1 << chunk_pos)) != 0 ? 1 : 0;
    }
    
    
    public int[][][] getState() {
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
	
    		if ( ((feedback_to_clauses[clause_chunk] & (1 << clause_chunk_pos) ) == 0) || (drop_clause[clause_chunk] & (1 << clause_chunk_pos)) != 0 ) {
				continue;
			}
    		
  
    		if ((2*target-1) * (1 - 2 * (j & 1)) == -1) {
    			if ((clause_output[clause_chunk] & (1 << clause_chunk_pos)) != 0) {
    				// Type II Feedback

					if (weighted && clause_weights[j] > 1) {
						clause_weights[j]--;
					}
    				
    				for (int k = 0; k < la_chunks; ++k) {
    					    					
    					inc(j, k, (~Xi[clause_patch[j]*la_chunks + k]) & (~ta_state[j][k][state_bits-1]));
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
    		 				inc(j, k, Xi[clause_patch[j]*la_chunks + k]);
    					else
    						inc(j, k, Xi[clause_patch[j]*la_chunks + k] & (~feedback_to_la[k]));
    					
    		 			dec(j, k, (~Xi[clause_patch[j]*la_chunks + k]) & feedback_to_la[k]);
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
			
			if( ((clause_output[clause_chunk] & (1 << clause_pos)) != 0) && j % 2 == 0) {	
				
				for (k = 0; k < nFeatures; k++) {
					
			    	int la_chunk = k / INT_SIZE;
			    	int chunk_pos = k % INT_SIZE;
					
					action_include = tm_action(j, k);
					action_include_negated = tm_action(j, nFeatures + k);
					
					if(action_include == 1 && ((Xi[clause_patch[j]*la_chunks + la_chunk] & (1 << chunk_pos)) != 0)) {
						local_feature_strength[k]++;
					}
					
					if(action_include_negated == 1 && ((Xi[clause_patch[j]*la_chunks + la_chunk] & (1 << chunk_pos)) == 0) ) {
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
	public int[] riskPrediction(int[] Xi, boolean positive_polarity) {
		
		int j, k;
		int action_include, action_include_negated;
	
		int[] risk_strength = new int[2*nFeatures];
		int positive_pol = 0;
		
		if(!positive_polarity) positive_pol = 1;
		
		for (j = 0; j < nClauses; j++) {
		  
			int clause_chunk = j / 32;
			int clause_pos = j % 32;
			
			if( ((clause_output[clause_chunk] & (1 << clause_pos)) != 0) && j % 2 == positive_pol ) {	
				
				for (k = 0; k < nFeatures; k++) {
					
			    	int la_chunk = k / INT_SIZE;
			    	int chunk_pos = k % INT_SIZE;
					
					action_include = tm_action(j, k);
					action_include_negated = tm_action(j, nFeatures + k);
					
					if(action_include == 1 && ((Xi[clause_patch[j]*la_chunks + la_chunk] & (1 << chunk_pos)) != 0)) {
						risk_strength[k]++;
					}
					
					if(action_include_negated == 1 && ((Xi[clause_patch[j]*la_chunks + la_chunk] & (1 << chunk_pos)) == 0) ) {
						risk_strength[nFeatures + k]++;
					}
				}	
			}
		}		
		return risk_strength;		
	}
	
	
	
	
	
	
	
	
	/**
	 * Explainability interface goes here
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
				feature_strength[2*k] += tm_action(j, nFeatures +k);
			}	
		}		
		return feature_strength;
	}
	
	public int[] computeWeightedFeatureStrength() {
		
		int[] feature_strength = new int[2*nFeatures];
		
		for(int k = 0; k < nFeatures; k++) {
			for(int j = 0; j < nClauses; j++) {
			
				feature_strength[k] += tm_action(j, k)*clause_weights[j];
				feature_strength[2*k] += tm_action(j, nFeatures +k)*clause_weights[j];
			}	
		}		
		return feature_strength;
	}
	
	
	
	/**
	 * Computes the feature strength per clause for organizing clause patterns
	 */
	public void computeWeightedFeatureStrengthByClause() {
		
		clause_feature_strength = new int[nClauses][2*nFeatures];
		
		for(int j = 0; j < nClauses; j++) {
			
			for(int k = 0; k < nFeatures; k++) {
				clause_feature_strength[j][k] += tm_action(j, k);
				clause_feature_strength[j][2*k] += tm_action(j, nFeatures + k);
			}			
		}
	}
	
	
	
	/**
	 * Computes for a given position in time
	 * @param index from 0 < index < dim_y - patch_dim_y
	 * @return
	 */
	public int[] computeConditionalFeatureStrength(int index) {
		
		int dim_x = encoder.getDimX();
		int dim_y = encoder.getDimY();
		int dim_z = encoder.getDimZ();
		
		int patch_dim_y = encoder.getDimPatchY();
		int patch_dim_x = encoder.getDimPatchX();
		
		index = index%(dim_y - patch_dim_y);
		
		int[] index_patch = new int[patch_dim_y];
		for(int j = 0; j < nClauses; j++) {
			
			if(tm_action(j, index) == 1) {
				
				for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
					for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
						for (int z = 0; z < dim_z; ++z) {					
							
							int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;
							index_patch[p_y] += tm_action(j, patch_pos)*clause_weights[j];
						}
					}
				}		
			}
		}
		return index_patch;		
	}
	
	
	public ClauseDescriptor outputClause(int clause_number) {
		
		int dim_x = encoder.getDimX();
		int dim_y = encoder.getDimY();
		int dim_z = encoder.getDimZ();
		
		int patch_dim_y = encoder.getDimPatchY();
		int patch_dim_x = encoder.getDimPatchX();
		
		int max_threshold = 0;
		int min_threshold = dim_y-1;
		
		ClauseDescriptor clause = new ClauseDescriptor();
		
		for(int index = 0; index < dim_y - patch_dim_y; index++) {
			
			if(tm_action(clause_number, index) == 1 && max_threshold < index) {
				max_threshold = index;
			}
			if(tm_action(clause_number, nFeatures + index) == 1 && min_threshold > index) {
				min_threshold = index;
			}	
		}
		int clause_location = max_threshold;
		clause.setInterval(max_threshold, min_threshold);
		

		float[][] output = new float[2][patch_dim_y];
		
		for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
			
			max_threshold = 0;
			min_threshold = patch_dim_x-1;
			
			for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
				for (int z = 0; z < dim_z; ++z) {					
					
					int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;
					
					if(tm_action(clause_number, patch_pos) == 1 && max_threshold < p_x) {
						max_threshold = p_x;
					}
					if(tm_action(clause_number, nFeatures + patch_pos) == 1 && min_threshold > p_x) {
						min_threshold = p_x;
					}
					
				}
			}
		}		
		clause.setFeature_strength(output);
			
		return clause; 
	}
	
	/**
	 * Returns the most n relevant clauses based on clause weighting
	 * We sort the clause weights, and return the top n clauses that 
	 * are contributing to the positive output. 
	 * 
	 * Only used if this particular Tsetlin when the output is largest
	 * output of all classes
	 * @param n
	 * @return
	 */
	public ClauseDescriptor[] getTopPatterns(int n) {
		
		clause_index = new int[clause_weights.length];
        for(int i = 0; i < clause_index.length; i++) {
        	clause_index[i] = i;
        }
		
		int[] copy_clause_weights = ArrayUtils.clone(clause_weights);
		QuickSort.sort(copy_clause_weights, clause_index);
        ArrayUtils.reverse(copy_clause_weights);
        ArrayUtils.reverse(clause_index);
        
        ClauseDescriptor[] top_clauses = new ClauseDescriptor[n];
        
        for(int i = 0; i < n; i++) {
        	if(clause_index[i]%2 == 0) {
        		top_clauses[i] = outputClause(clause_index[i]);
        	}  	
        }
        
        return top_clauses;		
	}
	
	public int[] getSortedClauses() {
		
		clause_index = new int[clause_weights.length];
        for(int i = 0; i < clause_index.length; i++) {
        	clause_index[i] = i;
        }
		
		int[] copy_clause_weights = ArrayUtils.clone(clause_weights);
		QuickSort.sort(copy_clause_weights, clause_index);
        ArrayUtils.reverse(copy_clause_weights);
        ArrayUtils.reverse(clause_index);
        
        return clause_index;		
	}
	
	
	
	public int[] getGlobalFeatureImportance() {
		return computeWeightedFeatureStrength();
	}
	
	/**
	 * Get the encoder reference
	 * @return
	 */
	public ConvolutionEncoder getEncoder() {
		return encoder;
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
	
	public int getNumberOfPatches() {
		return number_of_patches;
	}
	
	public int getNumberOfTAChunks() {
		return la_chunks;
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
	

	public int getT() {
		return T;
	}
	
	public static void main(String[] args) throws Exception {
		

 		
	}

	public int getThreshold() {
		return T;
	}

	public int[] getFeedback() {
		
		return feedback_to_clauses;
	}

	public int getState_bits() {
		// TODO Auto-generated method stub
		return state_bits;
	}

	public int getNumberOfFeatures() {
		return nFeatures;
	}

	public int getAction(int clause_number, int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getClass_probability() {
		return class_probability;
	}

	public void setClass_probability(double class_probability) {
		this.class_probability = class_probability;
	}


	public float[] getClause_patch_coverage() {
		return clause_patch_coverage;
	}
	
	public int getnClauses() {
		return nClauses;
	}

	public void setnClauses(int nClauses) {
		this.nClauses = nClauses;
	}
	

	public int[][] getClause_feature_strength() {
		return clause_feature_strength;
	}

	public void setClause_feature_strength(int[][] clause_feature_strength) {
		this.clause_feature_strength = clause_feature_strength;
	}
	
}
