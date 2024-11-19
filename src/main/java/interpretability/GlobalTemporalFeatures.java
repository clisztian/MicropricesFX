package interpretability;

import java.util.HashMap;

public class GlobalTemporalFeatures {

	private String time_name;
	private HashMap<String, BitRanges> global_features;
	private HashMap<String, float[]> feature_ranges;
	
	public GlobalTemporalFeatures(String time_name, String[] feature_names) {
		
		this.time_name = time_name;
		global_features = new HashMap<String, BitRanges>();
		
		for(int i = 0; i < feature_names.length; i++) {
			global_features.put(feature_names[i], new BitRanges());
		}
	}
	
	public void setValues(String name, float strength, float neg_strength, int[] pos_bits, int[] neg_bits, int[] max_range, int[] zero_range) {
		
		if(global_features.containsKey(name)) {
			global_features.get(name)
			   .setStrength(strength)
			   .setNeg_strength(neg_strength)
			   .setPos_bits(pos_bits)
			   .setNeg_bits(neg_bits)
			   .setMax_range(max_range)
			   .setZero_range(zero_range);
		}
	}
	
	public void setStrength(String name, float strength) {
		
		if(global_features.containsKey(name)) {
			global_features.get(name)
				.setStrength(strength);
		}	
	}
	
	class BitRanges {
		
		private float strength;
		private float neg_strength;
		private int[] pos_bits;
		private int[] neg_bits;
		private int[] max_range;
		private int[] zero_range;
				
		
		public int[] getPos_bits() {
			return pos_bits;
		}
		public BitRanges setPos_bits(int[] pos_bits) {
			this.pos_bits = pos_bits;
			return this;
		}
		public int[] getNeg_bits() {
			return neg_bits;
		}
		public BitRanges setNeg_bits(int[] neg_bits) {
			this.neg_bits = neg_bits;
			return this;
		}
		public int[] getMax_range() {
			return max_range;
		}
		public BitRanges setMax_range(int[] max_range) {
			this.max_range = max_range;
			return this;
		}
		public int[] getZero_range() {
			return zero_range;
		}
		public BitRanges setZero_range(int[] zero_range) {
			this.zero_range = zero_range;
			return this;
		}
		public float getStrength() {
			return strength;
		}
		public BitRanges setStrength(float strength) {
			this.strength = strength;
			return this;
		}
		public float getNeg_strength() {
			return neg_strength;
		}
		public BitRanges setNeg_strength(float neg_strength) {
			this.neg_strength = neg_strength;
			return this;
		}
		
	}


	public float getStrength(String name) {
		return global_features.get(name).getStrength();
	}


	public HashMap<String,BitRanges> getGlobal_features() {
		return global_features;
	}

	public BitRanges getBitRanges(String feature) {
		return global_features.get(feature);
	}

	public void setFeatureRanges(HashMap<String, float[]> feature_ranges) {
		this.setFeature_ranges(feature_ranges); 		
	}

	public HashMap<String, float[]> getFeature_ranges() {
		return feature_ranges;
	}

	public void setFeature_ranges(HashMap<String, float[]> feature_ranges) {
		this.feature_ranges = feature_ranges;
	}

	public String getTime_name() {
		return time_name;
	}

	public void setTime_name(String time_name) {
		this.time_name = time_name;
	}

}