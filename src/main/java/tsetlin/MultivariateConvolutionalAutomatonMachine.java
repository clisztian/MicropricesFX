package tsetlin;

import org.apache.commons.lang3.ArrayUtils;
import util.QuickSort;
import util.ReferenceMetrics;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;



/**
 * Provides and interface to the convolutional tsetlin machine
 * @author lisztian
 *
 */
public class MultivariateConvolutionalAutomatonMachine {

	
	private final int INT_SIZE = 32;
	private int nClauses;
	private int nClasses;
	private int nFeatures;
	private int T;
	private boolean append_negated;
	private boolean indexed;
	private float drop_clause_p;
	
	private ConvolutionEncoder encoder;
	
	private AutomataNode[] tm;
	Random rng;
	private double[] count_pos_features;
	private double[] count_neg_features;
	private double[][] feature_interpret;
	private String[][] feature_interpret_names;
	private double[][] neg_feature_interpret;
	private String[][] neg_feature_interpret_names;
	
	DecimalFormat df = new DecimalFormat("#.##");
	private int[] global_y_strength;
	private int[] global_x_position;
	private int[] global_patch;
	private int[] global_y_index;

	public MultivariateConvolutionalAutomatonMachine(ConvolutionEncoder encoder, int threshold, int nClasses, int nClauses, float max_specificity, boolean boost, float drop_clause_p) {
		
		this.encoder = encoder;
		this.nClauses = nClauses;
		this.nClasses = nClasses;

		this.drop_clause_p = drop_clause_p;
		this.indexed = true;
		this.append_negated = true;
		this.T = threshold;

		tm = new AutomataNode[nClasses];
				
		for(int i = 0; i < nClasses; i++) {
			tm[i] = new AutomataNode(encoder, threshold, nClauses, max_specificity, boost, drop_clause_p).initialize();
		}
		rng = new Random(21);
		
	}
	
	

	/******************************************/
	/*** Online Training of Tsetlin Machine ***/
	/******************************************/

	// The Tsetlin Machine can be trained incrementally, one training example at a time.
	// Use this method directly for online and incremental training.

	public int update(int Xi[], int target_class) {
		
		if(nClasses == 1) return update_regression(Xi, target_class);
		
		//System.out.println("Target class: " + target_class);
		tm[target_class].update(Xi, 1);

		int negative_target_class = rng.nextInt(nClasses);
		while (negative_target_class == target_class) {
			negative_target_class = rng.nextInt(nClasses);
		}

		tm[negative_target_class].update(Xi, 0);
		
		return predict(Xi);
	}


	public void drop_clause() {
		
		if(drop_clause_p > 0) {
			for(int i = 0; i < nClasses; i++) {
				tm[i].updateDropClause();
			}
		}	
	}
	
	
	private int update_regression(int Xi[], int target) {	
		return tm[0].update_regression(Xi, target);	
	}
	
	private int predict_regression(int[] X) {		
		return tm[0].score_regression(X);	
	}
	
	/**
	 * Returns the max class 
	 * @param X
	 * @return
	 */
	public int predict(int[] X) {
		
		if(nClasses == 1) return predict_regression(X);
		
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
	public int[] predict_interpret(int[] X) {
		
		int max_class_sum = tm[0].score(X);
		
		int max_class = 0;
		for (int i = 1; i < nClasses; i++) {	
			int class_sum = tm[i].score(X);
			if (max_class_sum < class_sum) {
				max_class_sum = class_sum;
				max_class = i;
			}
		}

		return tm[max_class].interpretablePrediction(X, max_class);
	}
	
	
	public static void output_digit(int Xi[])
	{
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

	 */
	public void fit(int X[][], int y[], int number_of_examples, int epochs) {
		for (int epoch = 0; epoch < epochs; epoch++) {
			// Add shuffling here...		
			for (int i = 0; i < number_of_examples; i++) {
				
				for(int k = 0; k < X[i].length; k++) {
					System.out.print(Integer.toUnsignedLong(X[i][k]) + " ");
				}
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
		
		int errors;
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
	
	
	public static void main(String[] args) throws Exception {
		
		int num_samples = 59000;
				
		int dimX = 28;
		int dimY = 28;
		int patchX = 10; 
		int patchY = 10;
		
		ConvolutionEncoder myEncoder = new ConvolutionEncoder(dimX, dimY, 1, patchX, patchY);
		
		

	}
	
	
	public AutomataNode getMachine(int myClass) {
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


	public int[] getConditionalFeatureStrength(int index, int myclass) {
		return tm[myclass].computeConditionalFeatureStrength(index);
	}
	

	public int getNumberClauses() {
		return nClauses;
	}



	public void setnClauses(int nClauses) {
		this.nClauses = nClauses;
	}



	public int getNumberFeatures() {
		return encoder.getNumber_of_features();
	}


	public int getNumberClasses() {
		return nClasses;
	}





	public int getThreshold() {
		// TODO Auto-generated method stub
		return tm[0].getThreshold();
	}



	public int getStateBits() {
		// TODO Auto-generated method stub
		return tm[0].getState_bits();
	}


	
	
	/**
	 * Computes all the global feature interpretability stats for a given class
	 * 
	 * @param myclass
	 */
	public void computeGlobalTimeSeriesFeatureImportance(int myclass) {
		

		int[] features = null;
		
	
		int dim_x = encoder.getDimX();
		int dim_y = encoder.getDimY();
		int dim_z = encoder.getDimZ();
		
		int patch_dim_y = encoder.getDimPatchY();
		int patch_dim_x = encoder.getDimPatchX();
		
		features = getWeightedGlobalFeatureImportance(myclass);
				
		global_patch = new int[patch_dim_y];
		global_y_strength = new int[dim_y - patch_dim_y];
			
		// Decode y coordinate of patch into feature vector 
		for (int y = 0; y < dim_y - patch_dim_y; y++) {
			global_y_strength[y] = features[y];
		}


		// Encode patch content into feature vector
		for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
			for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
				for (int z = 0; z < dim_z; ++z) {					
					int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;
					global_patch[p_y] += features[patch_pos];
				}
			}
		}		
	}
	
	public ClauseDescriptor[] getTopClausePatterns(int myclass) {
		return tm[myclass].getTopPatterns(5);
	}

	
	
	/**
	 * Gets the sorted importance of location
	 * @return
	 */
	public void computeGlobalPositionStrengthSorted(int myclass) {
		
		computeGlobalTimeSeriesFeatureImportance(myclass);
		
		global_y_index = new int[global_y_strength.length];
        for(int i = 0; i < global_y_strength.length; i++) {
        	global_y_index[i] = i;
        }
        
        QuickSort.sort(global_y_strength, global_y_index);
        ArrayUtils.reverse(global_y_strength);
        ArrayUtils.reverse(global_y_index);
	}
	
	public int[] getGlobalPositionStrength() {
		return global_y_strength;
	}
	
	public int[] getGlobalPositionStrengthIndex() {
		return global_y_index;
	}
	
	/**
	 * Gets the accummulative feature importance patch
	 * @return
	 */
	public int[] getGlobalFeaturePatch() {
		return global_patch;
	}



	public ConvolutionEncoder getEncoder() {
		return encoder;
	}




	/**
	 * Gets the risk factors of any prediction, regardless of class
	 * Assumes the class 0 is the "negative, or bad class" (fraud, failure, disease) etc
	 * 
	 * Target/pred class is the class that was predicted. But we would like to see what the stakes are in
	 * predicting another class, alternative_class. 
	 * 
	 * This is estimated by computing the impact of the alternative class and combining it with the factors 
	 * negatively impacting the predicted class
	 * 
	 * @param Xi
	 * @return
	 */
	public int[] riskFactors(int[] Xi, int pred_class, int alt_class) {
				
		int[] risk_0 = tm[alt_class].riskPrediction(Xi, true);
		int[] risk_1 = tm[pred_class].riskPrediction(Xi, false);
				
		int result[] = new int[risk_0.length];
	    Arrays.setAll(result, i -> risk_0[i] + risk_1[i]);
		return result;
	}
	
	
	/**
	 * Compute the clause importance for every class
	 * 
	 * Usually recomputed computed after every Epoch completion
	 */
	public void computeClauseImportance() {
		
		for (int i = 0; i < nClasses; i++) {	
			tm[i].computeWeightedFeatureStrengthByClause();
		}
		
	}
	
	public int[][] getClauseImportance(int my_class) {
		return tm[my_class].getClause_feature_strength();
	}
	
}
