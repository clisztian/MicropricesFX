package encoders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 
 * Encodes real values to bit encoded array
 * 
 * If more than one type, can encode various types using total_number_feature_types
 * 
 * @author lisztian
 *
 */

public class RealEncoder implements Encoder<Float> {

	private int max_bits = 40;	
	private String feature_name;
	private int feature_rank;
	private int total_number_feature_types = 0; 
	private boolean log_transform = false;
	
	private ArrayList<Float> values;



	private  boolean toInt = false;


	private int number_of_features;
	private float[] all_split_values;
	
	/**
	 * Experiment name, rank and number of bits
	 * Rank gives the experiment classification for encoding purposes
	 * @param name
	 * @param bits
	 */
	public RealEncoder(String name, int rank, int bits) {
		this.feature_rank = rank;
		this.feature_name = name;
		this.max_bits = bits;		
	}
	
	
	/**
	 * Experiment name and number of bits
	 * @param name
	 * @param bits
	 */
	public RealEncoder(String name, int bits) {		
		this.feature_name = name;
		this.max_bits = bits;		
		
		values = new ArrayList<Float>();
	}

	public RealEncoder(int bits) {		
		this.max_bits = bits;		
	}
	
	public void setLogTransformed(boolean is) {
		log_transform = is;
	}
	
	/**
	 * Fits a uniform binarizer to the data with maximum max_bits
	 * This assumes the data is more or less distributed uniform across a range
	 * all_split_values
	 */
//	public void fit_uniform() {
//		
//		List<Float> uv = values.stream().distinct().collect(Collectors.toList());
//		Collections.sort(uv);
//			
//		List<Float> split_vals = new ArrayList<Float>();
//		if(uv.size() > max_bits) {
//				
//			float step_size = 1f*uv.size()/max_bits;
//			float pos = 0f;
//				
//			while((int)pos < uv.size() && split_vals.size() < max_bits) {
//
//				split_vals.add(log(uv.get((int)(pos))));		
//				pos += step_size;
//			}				
//		}
//		else split_vals = uv;
//		
//		/**
//		 * if discrepency, add dummy values at end
//		 */
//		if(split_vals.size() < max_bits) {
//			
//			int disc = max_bits - split_vals.size();
//			float val = split_vals.get(split_vals.size() - 1);
//			
//			for(int i = 0; i < disc; i++) {
//				split_vals.add(val + (float)i);
//			}
//		}
//		
//		all_split_values = new float[split_vals.size()];
//		for(int k = 0; k < all_split_values.length; k++) all_split_values[k] = split_vals.get(k);		
//		number_of_features = all_split_values.length;						
//	}
	

	@Override
	public void fit_uniform() {
		
		List<Float> uv = values.stream().distinct().collect(Collectors.toList());
		Collections.sort(uv);
		
		float min = log(uv.get(0));
		float max = log(uv.get(uv.size()-1));
		
		float delta = (max - min)/(max_bits-1);
		
		List<Float> split_vals = new ArrayList<Float>();
		
		float val = min;
		
		if(uv.size() > max_bits) {
			while (val < max) {		
				split_vals.add(val);
				val+= delta;		
			}
		}
		else split_vals = uv;
		
		all_split_values = new float[split_vals.size()];
		
		for(int k = 0; k < all_split_values.length; k++) all_split_values[k] = split_vals.get(k);		
		number_of_features = all_split_values.length;						
	}
	
	
	/**
	 * Fits a dynamic binarizer to the data with maximum max_bits
	 * Assumes the data is not distributed uniform
	 */
	@Override
	public void fit_dynamic() {
		
		
		List<Float> uv = values.stream().distinct().collect(Collectors.toList());
		Collections.sort(uv);
			
		List<Float> split_vals = new ArrayList<Float>();
		if(uv.size() > max_bits) {
				
			int bucket_size = (int)(uv.size()/max_bits);		
		    for(int i = 0; i < uv.size(); i++) {		    		
		    	if(i%bucket_size == 0) {
		    		split_vals.add(log(uv.get(i)));
		    	} 	
		    }	
		}
		else split_vals = uv;
	
		if(split_vals.size() > max_bits) {
			while(split_vals.size() > max_bits) {
				split_vals.remove(split_vals.size() - 1);
			}
		}
		
		/**
		 * if discrepency, add dummy values at end
		 */
		if(split_vals.size() < max_bits) {
			
			int disc = max_bits - split_vals.size();
			float val = split_vals.get(split_vals.size() - 1);
			
			for(int i = 0; i < disc; i++) {
				split_vals.add(val + (float)i);
			}
		}
			
		all_split_values = new float[split_vals.size()];
		for(int k = 0; k < all_split_values.length; k++) all_split_values[k] = split_vals.get(k);		
		number_of_features = all_split_values.length;
						
	}
	
	/**
	 * Fits a dynamic binarizer to the data with maximum max_bits
	 * Assumes the data is not distributed uniform
	 */
	@Override
	public void fit_dynamic(ArrayList<Float> uniquevals) {
		
		
		List<Float> uv = uniquevals.stream().distinct().collect(Collectors.toList());
		Collections.sort(uv);
			
		List<Float> split_vals = new ArrayList<Float>();
		if(uv.size() > max_bits) {
				
			int bucket_size = (int)(uv.size()/max_bits);		
		    for(int i = 0; i < uv.size(); i++) {		    		
		    	if(i%bucket_size == 0) {
		    		split_vals.add(log(uv.get(i)));
		    	} 	
		    }	
		}
		else split_vals = uv;
	
		if(split_vals.size() > max_bits) {
			while(split_vals.size() > max_bits) {
				split_vals.remove(split_vals.size() - 1);
			}
		}
		
		/**
		 * if discrepency, add dummy values at end
		 */
		if(split_vals.size() < max_bits) {
			
			int disc = max_bits - split_vals.size();
			float val = split_vals.get(split_vals.size() - 1);
			
			for(int i = 0; i < disc; i++) {
				split_vals.add(val + (float)i);
			}
		}

		all_split_values = new float[split_vals.size()];
		for(int k = 0; k < all_split_values.length; k++) all_split_values[k] = split_vals.get(k);		
		number_of_features = all_split_values.length;
						
	}
	
	
	
	/**
	 * Transform a raw value to bit_encoded
	 * @param myval
	 * @return
	 */
	private int[] _transform(float myval) {
		
		int[] bit_encoded = new int[number_of_features];
		
		for(int k = 0; k < number_of_features; k++) {
				bit_encoded[k] = (log(myval) >= all_split_values[k]) ? 1 : 0;											
		}		
		return bit_encoded;		
	}

	public int transformToInt(float myval) {

		int[] bit_encoded = new int[number_of_features];

		for(int k = 0; k < number_of_features; k++) {
			bit_encoded[k] = (log(myval) >= all_split_values[k]) ? 1 : 0;
			if(bit_encoded[k] == 0) return k;
		}
		return number_of_features;
	}


	
	/**
	 * Transform a raw value to bit_encoded
	 * @param myval
	 * @return
	 */
	private int[] _transform_experiment(int total_number_feature_types, float myval) {
		
		this.total_number_feature_types = total_number_feature_types;
		
		int[] bit_encoded = new int[total_number_feature_types + number_of_features];
		bit_encoded[feature_rank] = 1;
		
		
		for(int k = 0; k < number_of_features; k++) {
				bit_encoded[total_number_feature_types + k] = (log(myval) >= all_split_values[k]) ? 1 : 0;											
		}		
		return bit_encoded;		
	}
	


	public float[] getAll_split_values() {
		return all_split_values;
	}

	public String getFeature_name() {
		return feature_name;
	}

	public ArrayList<Float> getValues() {
		return values;
	}

	public void setValues(ArrayList<Float> values) {
		this.values = values;
	}

	private float log(float val) {
		return (float) (log_transform ? Math.log(val) : val);
	}


	public int getTotal_number_feature_types() {
		return total_number_feature_types;
	}


	public void setTotal_number_feature_types(int total_number_feature_types) {
		this.total_number_feature_types = total_number_feature_types;
	}

	/**
	 * Returns a transform of myval to bit ecoded form
	
	 * @param value
	 * @return
	 */
	@Override
	public int[] transform(Float value) {

		if(getTotal_number_feature_types() > 0) {
			return _transform_experiment(getTotal_number_feature_types(), value.floatValue());
		}
		else {
			return _transform(value.floatValue());
		}	
	}


	@Override
	public void addValue(Float value) {
		values.add(value);	
	}


	@Override
	public int getBitDimension() {

		if(number_of_features < max_bits) return number_of_features;
		else return max_bits;
		
	}


	@Override
	public Float decoder(int[] enc) {
		
		int index = 0;
		for(int i = 0; i < enc.length; i++) {
			
			if(enc[i] == 0) {
				index = i; 
				break;
			}
		}
		if(index == 0) index = 1;
		
		return all_split_values[index-1];
	}

	public boolean isToInt() {
		return toInt;
	}

	public void setToInt(boolean toInt) {
		this.toInt = toInt;
	}

	public String getStringSplitValue() {
		
		String splits = "";
		for(int i = 0; i < all_split_values.length; i++) {
			splits += all_split_values[i] + " ";
		}
		splits += "\n";
		
		return splits;		
	}


	public int getNumber_of_features() {
		return number_of_features;
	}
	
}
