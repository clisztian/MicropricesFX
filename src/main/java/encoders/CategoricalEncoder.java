package encoders;

import util.MutableInt;
import util.QuickSort;

import java.util.*;
import java.util.Map.Entry;

public class CategoricalEncoder implements Encoder<String> {

	private String feature_name;
	private int feature_rank;
	private int total_number_feature_types; 	
	private ArrayList<String> values;

	private HashMap<String, Integer> category_map;
	
	private int number_of_features;

	public CategoricalEncoder(String feature_name, int feature_rank, int total_number_feature_types) {
		
		this.setTotal_number_feature_types(total_number_feature_types);
		this.setFeature_name(feature_name);
		this.setFeature_rank(feature_rank);
		this.values = new ArrayList<String>();
	}
	

	public CategoricalEncoder(String feature_name) {		
		total_number_feature_types = 0;
		this.setFeature_name(feature_name);
		this.values = new ArrayList<String>();
	}
	
	@Override
	public void fit_uniform() {

		int count = 0;
		category_map = new HashMap<String,Integer>();

		if(values.isEmpty()) {
			return;
		}
		
		HashMap<String, MutableInt> cat_map = new HashMap<String, MutableInt>();
		for(String cat : values) {
			
			MutableInt cate = cat_map.get(cat);
			if (cate  == null) {
				cat_map.put(cat, new MutableInt());
			}
			else {
				cate.increment();
			}
		}
		
		HashMap final_map = QuickSort.sort_mutable(cat_map);	
		Set<String> keys = final_map.keySet();
        for(String k : keys) {
        	category_map.put(k, count);
        	count++;


        }
		
	}

	@Override
	public void fit_dynamic() {
		
		int count = 0;
		category_map = new HashMap<String,Integer>();
		
		if(values.isEmpty()) {
			return;
		}
		
		HashMap<String, MutableInt> cat_map = new HashMap<String, MutableInt>();
		for(String cat : values) {
			
			MutableInt cate = cat_map.get(cat);
			if (cate  == null) {
				cat_map.put(cat, new MutableInt());
			}
			else {
				cate.increment();
			}
		}
		
		HashMap final_map = QuickSort.sort_mutable(cat_map);	
		Set<String> keys = final_map.keySet();
        for(String k : keys) {
        	category_map.put(k, count);
        	count++;
        }
	}

	@Override
	public void fit_dynamic(ArrayList<String> list) {
		
		this.values = list;
		
		int count = 0;
		category_map = new HashMap<String,Integer>();
		
		if(values.isEmpty()) {
			return;
		}
		
		HashMap<String, MutableInt> cat_map = new HashMap<String, MutableInt>();
		for(String cat : values) {
			
			MutableInt cate = cat_map.get(cat);
			if (cate  == null) {
				cat_map.put(cat, new MutableInt());
			}
			else {
				cate.increment();
			}
		}
		
		HashMap final_map = QuickSort.sort_mutable(cat_map);	
		Set<String> keys = final_map.keySet();
        for(String k : keys) {
        	category_map.put(k, count);
        	count++;
        }
		
	}
	

	private int[] _transform(String myval) {
		
		int[] bit_encoded = new int[category_map.size()];
		
		int val = category_map.get(myval);
		bit_encoded[val] = 1;
				
		return bit_encoded;		
	}

	public int transformToInt(String myval) {

		return category_map.get(myval);
	}
	
	/**
	 * Transform a raw value to bit_encoded
	 * @param myval
	 * @return
	 */
	private int[] _transform_experiment(int total_number_experiment_types, String myval) {
		
		int[] bit_encoded = new int[total_number_experiment_types + number_of_features];
		bit_encoded[feature_rank] = 1;
		int val = category_map.get(myval);
		bit_encoded[feature_rank + val] = 1;
			
		return bit_encoded;		
	}
	

	@Override
	public int[] transform(String value) {
		
		if(getTotal_number_feature_types() > 0) {
			return _transform_experiment(getTotal_number_feature_types(), value);
		}
		else {
			return _transform(value);
		}	
	}


	
	

	@Override
	public void addValue(String value) {
		values.add(value);
	}


	public String getFeature_name() {
		return feature_name;
	}


	public void setFeature_name(String feature_name) {
		this.feature_name = feature_name;
	}


	public int getFeature_rank() {
		return feature_rank;
	}


	public void setFeature_rank(int feature_rank) {
		this.feature_rank = feature_rank;
	}


	public int getTotal_number_feature_types() {
		return total_number_feature_types;
	}


	public void setTotal_number_feature_types(int total_number_feature_types) {
		this.total_number_feature_types = total_number_feature_types;
	}


	public int getNumber_of_features() {
		return number_of_features;
	}


	public void setNumber_of_features(int number_of_features) {
		this.number_of_features = number_of_features;
	}


	public HashMap<String, Integer> getCategory_map() {
		return category_map;
	}


	@Override
	public int getBitDimension() {
		return category_map.size();
	}


	@Override
	public String decoder(int[] enc) {
		
		int index = -1;
		for(int i = 0; i < enc.length; i++) {
			if(enc[i] == 1) {
				index = i; 
				break;
			}
		}
		
		if(index == -1) {
			return "none";
		}
		
		return getKeyByValue(category_map, index);
	}
	

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	
}
