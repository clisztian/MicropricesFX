package tsetlin;

import dynamics.Evolutionize;
import encoders.CategoricalEncoder;
import encoders.Encoder;
import encoders.RealEncoder;
import encoders.TimeEncoder;
import interpretability.GlobalCategoricalFeatures;
import interpretability.GlobalRealFeatures;
import interpretability.GlobalTemporalFeatures;
import interpretability.Prediction;

import org.apache.commons.lang3.ArrayUtils;

import records.AnyRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 
 * Interface for decoding results into interpretability maps
 * 
 * Requires: 
 * 
 * RecordEncoder which holds the encoding rules for record type V
 * Multivariate Conv Automation object
 * 
 * Computes interpretability measures on 
 * 1) Temporal in form of where patch gains most influence 
 * 2) Spatial in terms of where in path most influential ranges are
 * 
 * @author lisztian
 *
 */

public class AutomataLearning<V> {

	private Random rng;
	private Evolutionize<V> evolution;
	private AutomataAtomLearning automaton;


	
	private int dim_y;
	private int patch_dim_y;
	private int dim_x;
	
	private int n_global_features;
	private int n_real_features;
	private int n_categorical_features;
	private int n_time_features;


	private int nClasses;

	
	private float drop_clause_p;
	
	private GlobalRealFeatures[][] real_features;
	private GlobalRealFeatures lag_features;
	private GlobalTemporalFeatures[] temporal_features;
	private GlobalCategoricalFeatures[] categorical_features;
	
	private GlobalRealFeatures risk_lag_features;
	private GlobalRealFeatures[][] risk_real_features;
	private GlobalTemporalFeatures[] risk_temporal_features;
	private GlobalCategoricalFeatures[] risk_categorical_features;
	


	
	
	/**
	 * Instantiates a decoder with a generic V val
	 * @param dim_y
	 * @param patch_dim_y
	 * @param dim_x
	 * @param val
	 */
	public AutomataLearning(int dim_y, int patch_dim_y, int dim_x, V val, int nClauses, int threshold,  float max_specificity, int nClasses, float drop_clause_p) {
		
		this.dim_x = dim_x;
		this.dim_y = dim_y;
		this.patch_dim_y = patch_dim_y;
		this.drop_clause_p = drop_clause_p;
		this.nClasses = nClasses;
		
		evolution = new Evolutionize(patch_dim_y, dim_y);		
		
		if(val instanceof AnyRecord) {
			evolution.initiate((AnyRecord)val, dim_x);
		}
		else evolution.initiate(val.getClass(), dim_x);
		
		evolution.initiateConvolutionEncoder();
		automaton = new AutomataAtomLearning(evolution.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, drop_clause_p); 	
		
		n_global_features = evolution.getEncoder().getEncode_maps().length;
		
		n_real_features = 0;
		n_categorical_features= 0;
		n_time_features = 0;
		
		for(int i = 0; i < n_global_features; i++) {
			
			if(evolution.getEncoder().getEncode_maps()[i] instanceof RealEncoder) n_real_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof TimeEncoder) n_time_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof CategoricalEncoder) n_categorical_features++;	
		}

		System.out.println("n_real_features: " + n_real_features + " " + n_global_features);

		rng = new Random();
	}

	public AutomataLearning(ConvolutionEncoder encoder, int nClauses, int threshold,  float max_specificity, int nClasses, float drop_clause_p) {

		this.dim_x = encoder.getNumber_of_features();
		this.dim_y = 1;
		this.patch_dim_y = 1;
		this.drop_clause_p = drop_clause_p;
		this.nClasses = nClasses;

		evolution = new Evolutionize(patch_dim_y, dim_y);

		automaton = new AutomataAtomLearning(encoder, threshold, nClasses, nClauses, max_specificity, true, drop_clause_p);

		n_global_features = dim_x;

		n_real_features = 0;
		n_categorical_features= 0;
		n_time_features = 0;

		rng = new Random();
	}



	public AutomataLearning(Evolutionize evolution, int nClauses, int threshold,  float max_specificity, int nClasses, float drop_clause_p) {

		this.dim_x = evolution.getEncoderDimension();
		this.dim_y = evolution.getWindow();
		this.patch_dim_y = 1;
		this.drop_clause_p = drop_clause_p;
		this.nClasses = nClasses;

		this.evolution = evolution;

		automaton = new AutomataAtomLearning(evolution.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, drop_clause_p);

		n_global_features = evolution.getEncoder().getEncode_maps().length;

		n_real_features = 0;
		n_categorical_features= 0;
		n_time_features = 0;

		for(int i = 0; i < n_global_features; i++) {

			if(evolution.getEncoder().getEncode_maps()[i] instanceof RealEncoder) n_real_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof TimeEncoder) n_time_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof CategoricalEncoder) n_categorical_features++;
		}


		rng = new Random();
	}



	/**
	 * Instantiates a decoder with a generic V val
	 * Assumes the dimension of each field in V is given by some constant dim_x
	 * @param val
	 * @param nClauses
	 * @param threshold
	 * @param max_specificity
	 * @param nClasses
	 * @param drop_clause_p
	 */
	public AutomataLearning(V val, int nClauses, int threshold,  float max_specificity, int nClasses, float drop_clause_p) {


		AnyRecord record = (AnyRecord)val;
		this.dim_x = record.getDim_x();

		this.dim_y = 1;
		this.patch_dim_y = 1;
		this.drop_clause_p = drop_clause_p;
		this.nClasses = nClasses;

		evolution = new Evolutionize(patch_dim_y, dim_y);

		if(val instanceof AnyRecord) {
			evolution.initiate((AnyRecord)val);
		}
		else throw new IllegalArgumentException("AutomataLearning: val must be of type AnyRecord");

		evolution.initiateConvolutionEncoder();
		automaton = new AutomataAtomLearning(evolution.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, drop_clause_p);

		n_global_features = evolution.getEncoder().getEncode_maps().length;

		n_real_features = 0;
		n_categorical_features= 0;
		n_time_features = 0;

		for(int i = 0; i < n_global_features; i++) {

			if(evolution.getEncoder().getEncode_maps()[i] instanceof RealEncoder) n_real_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof TimeEncoder) n_time_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof CategoricalEncoder) n_categorical_features++;
		}

		System.out.println("n_real_features: " + n_real_features + " " + n_global_features + " " + evolution.getEncoder().getBitDimension());

		rng = new Random();
	}



	public void resetAutomata(int nClauses, int threshold, float max_specificity, float drop_clause_p, int max_literals) {
		this.drop_clause_p = drop_clause_p;
		automaton = new AutomataAtomLearning(evolution.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, drop_clause_p);
		automaton.setMaxNumberOfLiterals(max_literals);
	}

	/**
	 * Instantiates a decoder with a generic V val 
	 * The time series version 
	 * 
	 * Encode different components of time
	 * @param dim_y
	 * @param patch_dim_y
	 * @param dim_x
	 * @param val
	 * @param nClauses
	 * @param threshold
	 * @param max_specificity
	 * @param nClasses
	 * @param hours
	 * @param day_of_month
	 * @param month_of_year
	 * @param week_of_year
	 */
	public AutomataLearning(int dim_y, int patch_dim_y, int dim_x, V val, int nClauses, int threshold,  float max_specificity, int nClasses, 
			boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year, float drop_clause_p) {
		
		this.dim_x = dim_x;
		this.dim_y = dim_y;
		this.patch_dim_y = patch_dim_y;
		this.drop_clause_p = drop_clause_p;
		
		evolution = new Evolutionize(patch_dim_y, dim_y);		
		
		if(val instanceof AnyRecord) {
			evolution.initiate((AnyRecord)val, dim_x);
		}
		else evolution.initiate(val.getClass(), dim_x, hours, day_of_month, month_of_year, week_of_year);
		
		evolution.initiateConvolutionEncoder();
		automaton = new AutomataAtomLearning(evolution.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, drop_clause_p); 	
		
		n_global_features = evolution.getEncoder().getEncode_maps().length;
		
		n_real_features = 0;
		n_categorical_features= 0;
		n_time_features = 0;
		
		for(int i = 0; i < n_global_features; i++) {
			
			if(evolution.getEncoder().getEncode_maps()[i] instanceof RealEncoder) n_real_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof TimeEncoder) n_time_features++;
			else if(evolution.getEncoder().getEncode_maps()[i] instanceof CategoricalEncoder) n_categorical_features++;	
		}
		rng = new Random();
	}

	public void setMaxNumberOfLiterals(int max_literals) {
		automaton.setMaxNumberOfLiterals(max_literals);
	}

	public void setNegativeFocusedSampling(boolean negative_focused_sampling) {
		automaton.setFocused_negative_sampling(negative_focused_sampling);
	}

	public void drop_clauses() {
		automaton.drop_clause();
	}
	
	/**
	 * Add a list of values in order to learn the feature's dimensions
	 * @param in_sample
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void add_fit(ArrayList<V> in_sample) throws IllegalArgumentException, IllegalAccessException {		
		for(int i = 0; i < in_sample.size(); i++) {				
			evolution.addValue(in_sample.get(i));
		}
		evolution.fit();		
	}

	public void add(V val) throws IllegalArgumentException, IllegalAccessException {
		evolution.add(val);
	}
	
	public void add(List<V> vals) throws IllegalArgumentException, IllegalAccessException {
		for(V val : vals) evolution.add(val);
	}
	
	/**
	 * Update with one value to classify
	 * @param val
	 * @param label
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public int update(V val, int label) throws IllegalArgumentException, IllegalAccessException {		
		evolution.add(val);

		int[] X = evolution.get_last_sample();

		return automaton.update(X, label);
	}
	
	
	/**
	 * Updates the evolution chain and predicts the output
	 * @param val
	 * @param label
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Prediction update_predict(V val, int label) throws IllegalArgumentException, IllegalAccessException {
		
		evolution.add(val);
		
		int update_pred = automaton.update(evolution.get_last_sample(), label);	
		int[] pred = automaton.predict_interpret(evolution.get_last_sample());	
		
		int myclass = pred[pred.length - 1];
		int alt_class = rng.nextInt(automaton.getNumberClasses());
		while(myclass == alt_class) {
			alt_class = rng.nextInt(automaton.getNumberClasses());
		}
		
		int[] allbits = ArrayUtils.subarray(pred, 0, pred.length-1);
		
		
		double probability = automaton.getMachine(myclass).getClass_probability();
		int[] risks = automaton.riskFactors(evolution.get_last_sample(), myclass, alt_class);
	
		Prediction prediction = new Prediction(myclass, probability);
		
		localRiskAndFeatureImportance(prediction, allbits, risks);
		
		return prediction;
		
	}


	
	/**
	 * Update with one sequence to classify
	 * @param vals
	 * @param label
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int update(ArrayList<V> vals, int label) throws IllegalArgumentException, IllegalAccessException {		
		
		for(int i = 0; i < vals.size(); i++) {
			evolution.add(vals.get(i));
		}		
		return automaton.update(evolution.get_last_sample(), label);	
	}

	public void fit(int[][] Xi, int[] yi) {
		automaton.fit(Xi, yi, false);
	}

	public int predict(int[] X) {
		return automaton.predict(X);
	}
	
	
	/**
	 * Add new single value and predict
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Prediction predict(V val) throws IllegalArgumentException, IllegalAccessException {
		
		evolution.add(val);
		int[] pred = automaton.predict_interpret(evolution.get_last_sample());	
		
		int myclass = pred[pred.length - 1];
		int alt_class = rng.nextInt(automaton.getNumberClasses());
		while(myclass == alt_class) {
			alt_class = rng.nextInt(automaton.getNumberClasses());
		}
		
		int[] allbits = ArrayUtils.subarray(pred, 0, pred.length-1);
		
		double probability = automaton.getMachine(myclass).getClass_probability();
		int[] risks = automaton.riskFactors(evolution.get_last_sample(), myclass, alt_class);
	
		Prediction prediction = new Prediction(myclass, probability);

		prediction.setClauseOutput(automaton.getClause_output_unwrapped(myclass));

		localRiskAndFeatureImportance(prediction, allbits, risks);
		
		return prediction;
			
	}
	
	
	/**
	 * Add new single value and predict
	 * @param vals
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Prediction predict(ArrayList<V> vals) throws IllegalArgumentException, IllegalAccessException {
		
		
		for(int i = 0; i < vals.size(); i++) {
			evolution.add(vals.get(i));
		}
		
		int[] pred = automaton.predict_interpret(evolution.get_last_sample());	
		
		int myclass = pred[pred.length - 1];
		int alt_class = rng.nextInt(automaton.getNumberClasses());
		while(myclass == alt_class) {
			alt_class = rng.nextInt(automaton.getNumberClasses());
		}
		
		int[] allbits = ArrayUtils.subarray(pred, 0, pred.length-1);
		
		double probability = automaton.getMachine(myclass).getClass_probability();
		int[] risks = automaton.riskFactors(evolution.get_last_sample(), myclass, alt_class);
	
		Prediction prediction = new Prediction(myclass, probability);
		
		localRiskAndFeatureImportance(prediction, allbits, risks);
		
		return prediction;
			
	}


	public void printEncoderNames() {

		//System.out.println("Printing encoder names");
		for(Encoder encoder : evolution.getEncoder().getEncode_maps()) {
			if(encoder instanceof RealEncoder) {
				RealEncoder real = (RealEncoder)encoder;
				//System.out.println(real.getFeature_name() + " dimension: " +  real.getNumber_of_features());
			}
		}
	}






	
	private void localFeatureImportance(int[] local_features) {
		
		int patch_dim_x = dim_x;
	
		int n_bit_features = automaton.getNumberFeatures();

		int[] temp_structure_pos = Arrays.copyOfRange(local_features, 0, dim_y - patch_dim_y);
		int[] temp_structure_neg = Arrays.copyOfRange(local_features, n_bit_features, n_bit_features + (dim_y - patch_dim_y));
		
		lag_features = decodeRealFeatures("Lag Importance", null, temp_structure_pos, temp_structure_neg); 
		
		real_features = new GlobalRealFeatures[patch_dim_y][n_real_features];
		temporal_features = new GlobalTemporalFeatures[patch_dim_y];
		categorical_features = new GlobalCategoricalFeatures[patch_dim_y];
		
		int p_x = 0;
		int patch_pos = 0;

		int n_real_features_count = 0;
		int start = (dim_y - patch_dim_y) + (dim_x - patch_dim_x);
		for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
			 
			 p_x = 0;
			 for(int feat_dim = 0; feat_dim < n_global_features; feat_dim++) { 
				
				 int bit_dim = evolution.getEncoder().getEncode_maps()[feat_dim].getBitDimension();
				 int[] local_pos = new int[bit_dim];
				 int[] local_neg = new int[bit_dim];
				 
				 for(int k = 0; k < bit_dim; k++) {
					 
				    patch_pos = start + p_y * patch_dim_x + p_x;	
					local_pos[k] = local_features[patch_pos];
					local_neg[k] = local_features[n_bit_features + patch_pos];							
				    p_x++;
				 }
				 
				 /**
				  * If its a real encoder, map it to a global real decoder
				  */
				 if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof RealEncoder) {
					 
					 real_features[p_y][n_real_features_count] = decodeRealFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (RealEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], 
							 local_pos, local_neg);
					 
					 n_real_features_count++;
				 }
				 /**
				  * If its a time encoder, map it to a global time decoder
				  */
				 else if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof TimeEncoder) {
					 
					 temporal_features[p_y] = decodeTemporalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (TimeEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], local_pos, local_neg);
				 }
				 /**
				  * If its a categorical encoder, map it to a global categorical decoder
				  */
				 else if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof CategoricalEncoder) {
					 
					 categorical_features[p_y] = decodeCategoricalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (CategoricalEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], local_pos, local_neg);
				 }
			 }
		}
		
		
	}
	

	

	private void localRiskAndFeatureImportance(Prediction prediction, int[] local_features, int[] risk_features) {
		
		int patch_dim_x = dim_x;
	
		int n_bit_features = automaton.getNumberFeatures();
		
		int[] temp_structure_pos = Arrays.copyOfRange(local_features, 0, dim_y - patch_dim_y);
		int[] temp_structure_neg = Arrays.copyOfRange(local_features, n_bit_features, n_bit_features + (dim_y - patch_dim_y));

		lag_features = decodeRealFeatures("Lag Importance", null, temp_structure_pos, temp_structure_neg); 	
		real_features = new GlobalRealFeatures[patch_dim_y][n_real_features];
		temporal_features = new GlobalTemporalFeatures[patch_dim_y];
		categorical_features = new GlobalCategoricalFeatures[patch_dim_y];
		
		risk_lag_features = decodeRealFeatures("Lag Risk", null, 
				ArrayUtils.subarray(risk_features, 0, dim_y - patch_dim_y), ArrayUtils.subarray(risk_features, n_bit_features, n_bit_features + (dim_y - patch_dim_y))); 	
		risk_real_features = new GlobalRealFeatures[patch_dim_y][n_real_features];
		risk_temporal_features = new GlobalTemporalFeatures[patch_dim_y];
		risk_categorical_features = new GlobalCategoricalFeatures[patch_dim_y];
		
		int p_x = 0;
		int patch_pos = 0;

		int n_real_features_count = 0;
		int start = (dim_y - patch_dim_y) + (dim_x - patch_dim_x);
		for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
			 
			 p_x = 0; n_real_features_count = 0;
			 for(int feat_dim = 0; feat_dim < n_global_features; feat_dim++) { 
				
				 int bit_dim = evolution.getEncoder().getEncode_maps()[feat_dim].getBitDimension();
				 int[] local_pos = new int[bit_dim];
				 int[] local_neg = new int[bit_dim];
				 
				 int[] risk_local_pos = new int[bit_dim];
				 int[] risk_local_neg = new int[bit_dim];
				 
				 for(int k = 0; k < bit_dim; k++) {
					 
				    patch_pos = start + p_y * patch_dim_x + p_x;	
				    
					local_pos[k] = local_features[patch_pos];
					local_neg[k] = local_features[n_bit_features + patch_pos];	
					
					risk_local_pos[k] = risk_features[patch_pos];
					risk_local_neg[k] = risk_features[n_bit_features + patch_pos];	
					
				    p_x++;
				 }
				 
				 /**
				  * If its a real encoder, map it to a global real decoder
				  */

				 if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof RealEncoder) {
					 
					 
					 real_features[p_y][n_real_features_count] = decodeRealFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (RealEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], 
							 local_pos, local_neg);
					 
					 risk_real_features[p_y][n_real_features_count] = decodeRealFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (RealEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], 
							 risk_local_pos, risk_local_neg);
					 
					 n_real_features_count++;
				 }
				 /**
				  * If its a time encoder, map it to a global time decoder
				  */
				 else if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof TimeEncoder) {
					 
					 temporal_features[p_y] = decodeTemporalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (TimeEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], local_pos, local_neg);
					 
					 risk_temporal_features[p_y] = decodeTemporalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (TimeEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], risk_local_pos, risk_local_neg);
				 }
				 /**
				  * If its a categorical encoder, map it to a global categorical decoder
				  */
				 else if(evolution.getEncoder().getEncode_maps()[feat_dim] instanceof CategoricalEncoder) {
					 
					 categorical_features[p_y] = decodeCategoricalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (CategoricalEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], local_pos, local_neg);
					 
					 risk_categorical_features[p_y] = decodeCategoricalFeatures(evolution.getEncoder().getField_names()[feat_dim], 
							 (CategoricalEncoder)evolution.getEncoder().getEncode_maps()[feat_dim], risk_local_pos, risk_local_neg);
				 }
			 }
		}
		
		prediction.setCategorical_features(categorical_features)
				  .setLag_features(lag_features)
				  .setReal_features(real_features)
				  .setTemporal_features(risk_temporal_features)
				  .setRisk_categorical_features(risk_categorical_features)
				  .setRisk_lag_features(risk_lag_features)
				  .setRisk_real_features(risk_real_features)
				  .setRisk_temporal_features(risk_temporal_features);
		
	}
	
	
	
	

	/**
	 * Decodes all the real features into various performance indicators 
	 * @param pos_cont
	 * @param neg_cont
	 * @return
	 */
	public GlobalRealFeatures decodeRealFeatures(String name, RealEncoder encoder, int[] pos_cont, int[] neg_cont) {
				
		GlobalRealFeatures global = new GlobalRealFeatures(name);

		float pos_mean = mean0(pos_cont);
		float neg_mean = mean0(neg_cont);
				
		int last_index = 0;
		int last_zero = 0; int first_zero = neg_cont.length - 1;
		int first_index = pos_cont.length - 1;
		int max = -1; 

		float[] split_values = new float[pos_cont.length];
		
		for(int k = 0; k < pos_cont.length; k++) {
			if(pos_cont[k] > max) {
				last_index = k; max = pos_cont[k];
			}
			if(pos_cont[k] == 0 && last_zero == 0) {
				last_zero = k;
			}
			split_values[k] = k*1f;
		}

		max = -1;
		for(int k = 0; k < neg_cont.length; k++) {
			if(neg_cont[k] > max) {
				first_index = k; max = neg_cont[k];
			}
			if(neg_cont[neg_cont.length - 1 - k] == 0 && first_zero == neg_cont.length - 1) {
				first_zero = k;
			}
		}
		//System.out.print(" [" + last_index  + ", " + first_index + "], [ " + first_zero + ", " + last_zero + "]\n");			
        global.setValues(name, pos_mean, neg_mean, pos_cont, neg_cont, new int[] {last_index, first_index}, new int[] {first_zero, last_zero});
        
        if(encoder != null) {
        	global.setFeature_ranges(encoder.getAll_split_values());	
        }
        else {
        	global.setFeature_ranges(split_values);
        }
		return global;		
	}
	
	
	/**
	 * Decodes the categorical features into the positive and negative strength categories
	 * @param name
	 * @param encoder
	 * @param pos_cont
	 * @param neg_cont
	 * @return
	 */
	public GlobalCategoricalFeatures decodeCategoricalFeatures(String name, CategoricalEncoder encoder, int[] pos_cont, int[] neg_cont) {
		
		GlobalCategoricalFeatures global = new GlobalCategoricalFeatures(name);
		
		int max = 0; 
		int neg_max = 0;
		float pos_mean = mean0(pos_cont);
		float neg_mean = mean0(neg_cont);
		
		float[] strengths = new float[] {pos_mean, neg_mean};
		
		int max_index_pos = -1;
		for(int k = 0; k < pos_cont.length; k++) {
			if(pos_cont[k] > max) {
				max = pos_cont[k];
				max_index_pos = k;
			}
		}
		
		int max_index_neg = -1;
		for(int k = 0; k < pos_cont.length; k++) {
			if(neg_cont[k] > neg_max) {
				neg_max = neg_cont[k];
				max_index_neg = k;
			}
		}
		
		int[] maxbitneg = new int[pos_cont.length];
		int[] maxbitpos = new int[pos_cont.length];
		
		if(max_index_pos >= 0) {
			maxbitpos[max_index_pos] = 1;
		}
		if(max_index_neg >= 0) {
			maxbitneg[max_index_neg] = 1;
		}
		
		String featpos = encoder.decoder(maxbitpos);
		String featneg = encoder.decoder(maxbitneg);
		
		global.setValues(name, featpos, featneg, pos_cont, neg_cont, strengths);
			
		return global;		
	}
	
	
	/**
	 * Decodes the temporal features into the various time components 
	 * 
	 * @param name
	 * @param encoder
	 * @param pos
	 * @param neg
	 * @return
	 */
	public GlobalTemporalFeatures decodeTemporalFeatures(String name, TimeEncoder encoder, int[] pos, int[] neg) {
		
		GlobalTemporalFeatures temporal_features = new GlobalTemporalFeatures(name, encoder.getTemporalNames());
		
		int[] dims = encoder.getSplit_dimensions();
		
		int start = 0; 
		for(int k = 0; k < dims.length; k++) {
			
			int[] pos_cont = new int[dims[k] - start];
			int[] neg_cont = new int[dims[k] - start];
			
			for(int i = start; i < dims[k]; i++) {
				pos_cont[i - start] = pos[i];
				neg_cont[i - start] = neg[i];
			}
			start = dims[k];
			
			float pos_mean = mean0(pos_cont);
			float neg_mean = mean0(neg_cont);
					
			int last_index = 0;
			int last_zero = 0; int first_zero = neg_cont.length - 1;
			int first_index = pos_cont.length - 1;
			int max = -1; 

			float[] split_values = new float[pos_cont.length];
			
			for(int j = 0; j < pos_cont.length; j++) {
				if(pos_cont[j] > max) {
					last_index = j; max = pos_cont[j];
				}
				if(pos_cont[j] == 0 && last_zero == 0) {
					last_zero = j;
				}
				split_values[j] = j*1f;
			}

			max = -1;
			for(int j = 0; j < neg_cont.length; j++) {
				if(neg_cont[j] > max) {
					first_index = j; max = neg_cont[j];
				}
				if(neg_cont[neg_cont.length - 1 - j] == 0 && first_zero == neg_cont.length - 1) {
					first_zero = j;
				}
			}
			System.out.print(" [" + last_index  + ", " + first_index + "], [ " + first_zero + ", " + last_zero + "]\n");			
			temporal_features.setValues(encoder.getTime_encoders().get(k).toString(), pos_mean, neg_mean, pos_cont, neg_cont, new int[] {last_index, first_index}, new int[] {first_zero, last_zero});
		}
		
		return temporal_features;
				
	}
	

	public void printNumberOfLiteralsForEachClause() {
		automaton.printNumberOfLiteralsOfEachClause();
	}
	
	
	
	
	private float mean0(int[] vec) {
		float sum = 0; 
		int count = 0;
		for(int i = 0; i < vec.length; i++) {
			sum+=vec[i];
			if(vec[i] != 0) {
				count++;
			}
		}
		if(count == 0) return 0;
		return sum/count;
	}
	
	
	
	public AutomataAtomLearning getAutomaton() {
		return automaton;
	}

	public void setAutomaton(AutomataAtomLearning automaton) {
		this.automaton = automaton;
	}





	public Evolutionize<V> getEvolution() {
		return evolution;
	}


	public void setEvolution(Evolutionize<V> evolution) {
		this.evolution = evolution;
	}



	public int getDim_y() {
		return dim_y;
	}



	public void setDim_y(int dim_y) {
		this.dim_y = dim_y;
	}



	public int getPatch_dim_y() {
		return patch_dim_y;
	}



	public void setPatch_dim_y(int patch_dim_y) {
		this.patch_dim_y = patch_dim_y;
	}



	public int getDim_x() {
		return dim_x;
	}



	public void setDim_x(int dim_x) {
		this.dim_x = dim_x;
	}

	public GlobalRealFeatures[][] getReal_features() {
		return real_features;
	}

	public GlobalRealFeatures getLag_features() {
		return lag_features;
	}

	public GlobalTemporalFeatures[] getTemporal_features() {
		return temporal_features;
	}

	public GlobalCategoricalFeatures[] getCategorical_features() {
		return categorical_features;
	}

	public GlobalRealFeatures getRisk_lag_features() {
		return risk_lag_features;
	}

	public GlobalRealFeatures[][] getRisk_Real_features() {
		return risk_real_features;
	}

	public GlobalTemporalFeatures[] getRisk_Temporal_features() {
		return risk_temporal_features;
	}

	public GlobalCategoricalFeatures[] getRisk_Categorical_features() {
		return risk_categorical_features;
	}



	public int[][] getWeights() {

		int[][] weights = new int[nClasses][automaton.getNumberClauses()];

		for(int i = 0; i < nClasses; i++) {
			weights[i] = automaton.getMachine(i).getClause_weights();
		}
		return weights;
	}


	public int getnClasses() {
		return nClasses;
	}
	
	
}
