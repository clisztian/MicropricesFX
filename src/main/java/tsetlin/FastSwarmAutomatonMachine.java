package tsetlin;

import util.ReferenceMetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;



public class FastSwarmAutomatonMachine {

	final int INT_SIZE = 32;
	
	private int nClauses;
	private int nClasses;
	private int nFeatures;
	private int la_chunks;
	private FastUnitAutomaton[] tm;
	Random rng;
	
	

	public FastSwarmAutomatonMachine(int threshold, int nClasses, int nFeatures, int nClauses, float max_specificity, boolean boost) {
		
		
		this.nClauses = nClauses;
		this.nClasses = nClasses;
		this.nFeatures = nFeatures;
		tm = new FastUnitAutomaton[nClasses];
		
		this.la_chunks = (2*nFeatures - 1)/INT_SIZE + 1;
		
		System.out.println("Automaton Stats: " + threshold + " " + nFeatures + " " + nClauses);
		
		for(int i = 0; i < nClasses; i++) {
			tm[i] = new FastUnitAutomaton(threshold, nFeatures, nClauses, max_specificity, boost).initialize();
		}
		rng = new Random(21);
		
	}


	public void fit(int[][] X, int[] y, boolean output_balancing) {



		int[] class_observed = new int[nClasses];
		int[] example_indexes = new int[nClasses];
		int example_counter = 0;

		ArrayList<Integer> example_indexes_list = new ArrayList<Integer>();
		//a shuffled list of indices for the examples X.length
		for(int i = 0; i < X.length; i++) {
			example_indexes_list.add(i);
		}
		Collections.shuffle(example_indexes_list);

		for(int i = 0; i < X.length; i++) {

			int e = example_indexes_list.get(i);

			if(output_balancing) {

				if(class_observed[y[e]] == 0) {
					example_indexes[y[e]] = e;
					class_observed[y[e]] = 1;
					example_counter++;
				}
			}
			else {
				example_indexes[example_counter] = e;
				example_counter++;
			}

			if(example_counter == nClasses) {

				example_counter = 0;

				for(int j = 0; j < nClasses; j++) {
					class_observed[j] = 0;
					int batch_example = example_indexes[j];
					int p = update(X[batch_example], y[batch_example]);
				}
			}
		}
	}



	/******************************************/
	/*** Online Training of Tsetlin Machine ***/
	/******************************************/

	// The Tsetlin Machine can be trained incrementally, one training example at a time.
	// Use this method directly for online and incremental training.

	public int update(int Xi[], int target_class) {
		
		//int[] Xi = bit_encode(X);
		
		//System.out.println("Target class: " + target_class);
		tm[target_class].update(Xi, 1);

		int negative_target_class = rng.nextInt(nClasses);
		while (negative_target_class == target_class) {
			negative_target_class = rng.nextInt(nClasses);
		}

		tm[negative_target_class].update(Xi, 0);
		
		return predict(Xi);
	}


	/**
	 * Returns the max class 
	 * @param X
	 * @return
	 */
	public int predict(int[] X) {
		
		int max_class_sum = tm[0].score(X);
		
		int max_class = 0;
		for (int i = 1; i < nClasses; i++) {	
			int class_sum = tm[i].score(X);
			if (max_class_sum < class_sum) {
				max_class_sum = class_sum;
				max_class = i;
			}
		}		
		return max_class;		
	}
	
	
	/**
	 * Returns the max class 
	 * @param X
	 * @return
	 */
	public int[] predict_interpret(int[] Xi) {
		
		//int[] Xi = bit_encode(X);
		
		int max_class_sum = tm[0].score(Xi);
		
		int max_class = 0;
		for (int i = 1; i < nClasses; i++) {	
			int class_sum = tm[i].score(Xi);
			if (max_class_sum < class_sum) {
				max_class_sum = class_sum;
				max_class = i;
			}
		}
		
		return tm[max_class].interpretablePrediction(Xi, max_class);
	}
	
	
	/**
	 * Gets the risk factors of any prediction, regardless of class
	 * Assumes the class 0 is the "negative, or bad class" (fraud, failure, disease) etc
	 * 
	 * 
	 * @param Xi
	 * @return
	 */
	public int[] riskFactors(int[] X) {
		
		int[] Xi = bit_encode(X);
		
		int[] risk_0 = tm[0].riskPredictionPositivePolarity(Xi);
		int[] risk_1 = tm[1].riskPredictionNegativePolarity(Xi);
				
		int result[] = new int[risk_0.length];
	    Arrays.setAll(result, i -> risk_0[i] + risk_1[i]);
		return result;
	}
	
	
	public static void output_digit(int Xi[]) {
		for (int y = 0; y < 28; y++) {
			for (int x = 0; x < 28; x++) {
				int chunk_nr = (x + y*28) / 32;
				int chunk_pos = (x + y*28) % 32;

				if ((Xi[chunk_nr] & (1 << chunk_pos)) != 0) {
					System.out.print("@");
				} else {
					System.out.print(".");
				}
			}
			System.out.print("\n");
		}
	}
	
	/**
	 * Batch training mode on examples with class given in 
	 * y and training data given in X
	 * @param X
	 * @param y
	 * @param number_of_examples
	 * @param epochs
	 * @param s
	 */
	public void fit(int X[][], int y[], int number_of_examples, int epochs) {
		for (int epoch = 0; epoch < epochs; epoch++) {
			// Add shuffling here...		
			for (int i = 0; i < number_of_examples; i++) {
				
				for(int k = 0; k < X[i].length; k++) {
					System.out.print(Integer.toUnsignedLong(X[i][k]) + " ");
				}
				System.out.println(y[i]);
				this.update(X[i], y[i]);
			}
		}
	}

	
	public float evaluate(int X[][], int y[], int number_of_examples) {
		
		int errors;
		int max_class;
		int max_class_sum;

		errors = 0;
		for (int l = 0; l < number_of_examples; l++) {
			/******************************************/
			/*** Identify Class with Largest Output ***/
			/******************************************/

			max_class_sum = tm[0].score(X[l]);
			max_class = 0;
			for (int i = 1; i < nClasses; i++) {	
				int class_sum = tm[i].score(X[l]);
				if (max_class_sum < class_sum) {
					max_class_sum = class_sum;
					max_class = i;
				}
			}

			
			if (max_class != y[l]) {
				errors += 1;
				
				//System.out.println(l + " Max_class = " + max_class + " " + y[l]);
			}
		}
		
		return 1f - 1f * errors / (float)number_of_examples;
	} 
	
	public ReferenceMetrics evaluateMetrics(int X[][], int y[]) {
		
		int max_class;
		int max_class_sum;
		int number_of_examples = y.length;


		int count_true = 0;
		int count_true_positive = 0;
		int count_false_negative = 0;
		int count_false = 0;;
		int count_true_negative = 0;
		int count_false_positive = 0;
		
		for (int l = 0; l < number_of_examples; l++) {
			/******************************************/
			/*** Identify Class with Largest Output ***/
			/******************************************/

			max_class_sum = tm[0].score(X[l]);
			max_class = 0;
			for (int i = 1; i < nClasses; i++) {	
				int class_sum = tm[i].score(X[l]);
				if (max_class_sum < class_sum) {
					max_class_sum = class_sum;
					max_class = i;
				}
			}
			int prediction = max_class;

		
			if(y[l] == 1) {
				count_true++;
				
				if(prediction == 1) {
					count_true_positive++;
				}
				else {
					count_false_negative++;
				}
			}
			else {
				count_false++;
				if(prediction == 0) {
					count_true_negative++;
				}
				else {
					count_false_positive++;
				}
			}
		}	
	
		float Xp = 1f*count_true_positive/count_true;
		float Y = 1f*count_true_negative/count_false;
		float Z = 1f*count_false_negative/count_true;
		float Q = 1f*count_false_positive/count_false;
		
		ReferenceMetrics metrics = new ReferenceMetrics();
		metrics.addMetrics(Xp, Y, Q, Z);
		
		return metrics;
		
	} 
	
	
	public int action(int myClass, int clause, int ta) {
		return tm[myClass].tm_action(clause, ta);
	}
	
	
	
	
	public FastUnitAutomaton getMachine(int myClass) {
		return tm[myClass];
	}

	
	public void reset_clause_output(int myClass) {
		tm[myClass].reset_clause_output();
	}
	

	/********************************************/
	/*** Evaluate the Trained Tsetlin Machine ***/
	/********************************************/

	public float evaluate(int[][] X, int y[]) {
		
		int errors;
		int max_class;
		int max_class_sum;
		int number_of_examples = y.length;

		errors = 0;
		for (int l = 0; l < number_of_examples; l++) {
			/******************************************/
			/*** Identify Class with Largest Output ***/
			/******************************************/

			max_class_sum = tm[0].score(X[l]);
			max_class = 0;
			for (int i = 1; i < nClasses; i++) {	
				int class_sum = tm[i].score(X[l]);
				if (max_class_sum < class_sum) {
					max_class_sum = class_sum;
					max_class = i;
				}
			}

			if (max_class != y[l]) {
				errors += 1;
			}
		}
		
		return 1f - 1f * errors / (float)number_of_examples;
	}

	/**
	 * Returns the feature importance for the given class
	 * @param myclass
	 * @return
	 */
	public int[] getGlobalFeatureImportance(int myclass) {
		return tm[myclass].computeFeatureStrength();		
	}

	/**
	 * Returns the feature importance for the given class
	 * @param myclass
	 * @return
	 */
	public int[] getWeightedGlobalFeatureImportance(int myclass) {
		return tm[myclass].computeWeightedFeatureStrength();
	}

	/**
	 * Returns the feature importance for the given class
	 * @param myclass
	 * @return
	 */
	public int[] getWeightedGlobalFeatureImportance(int myclass, double top) {		
		
		int mytop = (int)(tm[myclass].getNClauses()*top);
		return tm[myclass].computeWeightedFeatureStrength(mytop);
	}

	public int getNumberClauses() {
		return nClauses;
	}



	public void setnClauses(int nClauses) {
		this.nClauses = nClauses;
	}



	public int getNumberFeatures() {
		return nFeatures;
	}


	public int getNumberClasses() {
		return nClasses;
	}

	public void setnFeatures(int nFeatures) {
		this.nFeatures = nFeatures;
	}



	public int getThreshold() {
		// TODO Auto-generated method stub
		return tm[0].getThreshold();
	}



	public int getStateBits() {
		// TODO Auto-generated method stub
		return tm[0].getState_bits();
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

	
	
}
