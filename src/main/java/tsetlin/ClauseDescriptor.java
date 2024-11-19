package tsetlin;

import java.text.DecimalFormat;

public class ClauseDescriptor {

	private int max_threshold;
	private int min_threshold;
	private float[][] feature_strength;
	DecimalFormat df;
	
	public ClauseDescriptor() {
		df = new DecimalFormat("#.##");
	}
	
	
	public void setInterval(int max_threshold, int min_threshold) {
		this.setMax_threshold(max_threshold);
		this.setMin_threshold(min_threshold);
	}


	
	public int getMax_threshold() {
		return max_threshold;
	}


	public void setMax_threshold(int max_threshold) {
		this.max_threshold = max_threshold;
	}


	public int getMin_threshold() {
		return min_threshold;
	}


	public void setMin_threshold(int min_threshold) {
		this.min_threshold = min_threshold;
	}


	public float[][] getFeature_strength() {
		return feature_strength;
	}


	/**
	 * Sets the feature strength in terms of the value ranges
	 * for each time step in the given patch
	 * 
	 * minimum value in feature_strength[0] 
	 * maximum value in feature_strength[1]
	 * @param feature_strength
	 */
	public void setFeature_strength(float[][] feature_strength) {
		this.feature_strength = feature_strength;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Interval: " + max_threshold + " " + min_threshold + "\n");
		for(int i = 0; i < feature_strength[0].length; i++) {
			sb.append(df.format(feature_strength[0][i]) + " " + df.format(feature_strength[1][i]) + "\n");
		}	
		return sb.toString();		
	}
	
	
}
