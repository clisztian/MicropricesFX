package interpretability;



public class GlobalCategoricalFeatures {

	private String feature_name;

	private float[] feature_strengths;
	private String most_impactful_feature_pos;
	private String most_impactful_feature_neg;
	
	public GlobalCategoricalFeatures(String feature_name) {		
		this.feature_name = feature_name;		
	}
	
	public void setValues(String name, String strength, String neg_strength, int[] pos_bits, int[] neg_bits, float[] feature_ranges) {
		
		this.feature_strengths = feature_ranges;
		this.most_impactful_feature_pos = strength;
		this.most_impactful_feature_neg = neg_strength;
		
	}
		
	






	public float[] getFeature_ranges() {
		return feature_strengths;
	}

	public void setFeature_ranges(float[] feature_ranges) {
		this.feature_strengths = feature_ranges;
	}

	public String getFeatureName() {
		return feature_name;
	}

	public void setName(String name) {
		this.feature_name = name;
	}

	public String getMost_impactful_feature_neg() {
		return most_impactful_feature_neg;
	}

	public void setMost_impactful_feature_neg(String most_impactful_feature_neg) {
		this.most_impactful_feature_neg = most_impactful_feature_neg;
	}

	public String getMost_impactful_feature_pos() {
		return most_impactful_feature_pos;
	}

	public void setMost_impactful_feature_pos(String most_impactful_feature_pos) {
		this.most_impactful_feature_pos = most_impactful_feature_pos;
	}

}
