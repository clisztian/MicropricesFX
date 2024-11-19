package interpretability;

public class Prediction {

	
	
	private double probability;
	private int pred_class;
	private float[] pred_probabilities;
	private float regression_prediction;
	
	private GlobalRealFeatures[][] real_features;
	private GlobalRealFeatures lag_features;
	private GlobalTemporalFeatures[] temporal_features;
	private GlobalCategoricalFeatures[] categorical_features;

	private GlobalRealFeatures risk_lag_features;
	private GlobalRealFeatures[][] risk_real_features;
	private GlobalTemporalFeatures[] risk_temporal_features;
	private GlobalCategoricalFeatures[] risk_categorical_features;



	private int[] clause_output_unwrapped;


	public Prediction(int pred_class, double probability) {
		
		this.pred_class = pred_class;
		this.probability = probability;
		
	}
	
	
	public double getProbability() {
		return probability;
	}
	
	public Prediction setProbability(double probability) {
		this.probability = probability;
		return this;
	}

	public int getPred_class() {
		return pred_class;
	}

	public Prediction setPred_class(int pred_class) {
		this.pred_class = pred_class;
		return this;
	}

	public GlobalRealFeatures[][] getReal_features() {
		return real_features;
	}

	public Prediction setReal_features(GlobalRealFeatures[][] real_features) {
		this.real_features = real_features;
		return this;
	}

	public GlobalRealFeatures getLag_features() {
		return lag_features;
	}

	public Prediction setLag_features(GlobalRealFeatures lag_features) {
		this.lag_features = lag_features;
		return this;
	}

	public GlobalTemporalFeatures[] getTemporal_features() {
		return temporal_features;
	}

	public Prediction setTemporal_features(GlobalTemporalFeatures[] temporal_features) {
		this.temporal_features = temporal_features;
		return this;
	}

	public GlobalCategoricalFeatures[] getCategorical_features() {
		return categorical_features;
	}

	public Prediction setCategorical_features(GlobalCategoricalFeatures[] categorical_features) {
		this.categorical_features = categorical_features;
		return this;
	}

	public GlobalRealFeatures getRisk_lag_features() {
		return risk_lag_features;
	}

	public Prediction setRisk_lag_features(GlobalRealFeatures risk_lag_features) {
		this.risk_lag_features = risk_lag_features;
		return this;
	}

	public GlobalRealFeatures[][] getRisk_real_features() {
		return risk_real_features;
	}

	public Prediction setRisk_real_features(GlobalRealFeatures[][] risk_real_features) {
		this.risk_real_features = risk_real_features;
		return this;
	}

	public GlobalTemporalFeatures[] getRisk_temporal_features() {
		return risk_temporal_features;
	}

	public Prediction setRisk_temporal_features(GlobalTemporalFeatures[] risk_temporal_features) {
		this.risk_temporal_features = risk_temporal_features;
		return this;
	}

	public GlobalCategoricalFeatures[] getRisk_categorical_features() {
		return risk_categorical_features;
		
	}

	public Prediction setRisk_categorical_features(GlobalCategoricalFeatures[] risk_categorical_features) {
		this.risk_categorical_features = risk_categorical_features;
		return this;
	}


	public float getRegression_prediction() {
		return regression_prediction;
	}


	public void setRegression_prediction(float regression_prediction) {
		this.regression_prediction = regression_prediction;
	}


	public float[] getPred_probabilities() {
		return pred_probabilities;
	}


	public void setPred_probabilities(float[] pred_probabilities) {
		this.pred_probabilities = pred_probabilities;
	}

	public int[] getClause_output_unwrapped() {
		return clause_output_unwrapped;
	}
    public void setClauseOutput(int[] clause_output_unwrapped) {
		this.clause_output_unwrapped = clause_output_unwrapped;
    }
}
