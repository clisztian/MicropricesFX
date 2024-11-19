package records;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic placeholder for a record from csv or database
 * @author lisztian
 *
 */
public class AnyRecord {

	private Object[] values;
	private String[] field_names = null;



	private int[] field_dim = null;
	private String label_name = null;

	private Type[] type = null;
	
	private String[] meta_names = null;	
	private Integer label = null;

	private LinkedHashMap<String, String[]> value_map = null;

	public void setDim_x(int dim_x) {
		this.dim_x = dim_x;
	}

	/*
	 * Dimension of the data
	 */
	private int dim_x;

	public String getLabel_string() {
		return label_string;
	}

	public void setLabel_string(String label_string) {
		this.label_string = label_string;
	}

	private String label_string = null;

	private Map<String, Object> map;
	
	public AnyRecord() {
		field_names = null;
	}



	public AnyRecord(int label) {
		this.label = label;
	}
	
	
	public AnyRecord(String[] names) {
		setField_names(names);
	}
	
	public AnyRecord(Object[] values) {
		setValues(values);
	}
	
	public AnyRecord(String[] names, Object[] values) {
		setField_names(names);
		setValues(values);
	}

	//AnyRecord that initializes with value_map
	public AnyRecord(LinkedHashMap<String, String[]> value_map) {
		this.value_map = value_map;
	}
	
	public Object[] getValues() {
		return values;
	}
	public void setValues(Object[] values) {
		this.values = values;
	}
	public String[] getField_names() {
		return field_names;
	}
	public void setField_names(String[] field_names) {
		this.field_names = field_names;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Value: ");
		
		for(int i = 0; i < values.length; i++) {
			sb.append(values[i].toString() + ", ");
		}

		sb.append("Label: " + label);
		
		return sb.toString();
		
	}

	public String toLong() {

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < values.length; i++) {
			sb.append((int)  Float.parseFloat(values[i].toString()));
		}
		return sb.toString();

	}

	public Type[] getType() {
		return type;
	}

	public void setType(Type[] type) {
		this.type = type.clone();
	}
	
	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}
	
	public String[] getMeta_names() {
		return meta_names;
	}

	public void setMeta_names(String[] meta_names) {
		this.meta_names = meta_names;
	}

	public void setMap(Map<String, Object> map) {
		this.map=map;
	}

	public String getLabel_name() {
		return label_name;
	}

	public void setLabel_name(String label_name) {
		this.label_name = label_name;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void add(String name, String[] valSet) {
		value_map.put(name, valSet);
	}

	//commit the value_map to the record with keys as field_names
	public void commit() {

		dim_x = 0;
		field_names = new String[value_map.size()];
		field_dim = new int[value_map.size()];
		int i = 0;
		for(String key : value_map.keySet()) {
			field_names[i] = key;
			String[] vals = value_map.get(key);
			dim_x += vals.length;
			field_dim[i] = vals.length;
			i++;
		}
		values = new Double[field_names.length];
		for(int j = 0; j < field_names.length; j++) {
			values[j] = 0.0;
		}
	}

	public void commitCategories() {

		dim_x = 0;
		field_names = new String[value_map.size()];
		field_dim = new int[value_map.size()];
		int i = 0;
		for(String key : value_map.keySet()) {
			field_names[i] = key;
			String[] vals = value_map.get(key);
			dim_x += vals.length;
			field_dim[i] = vals.length;
			i++;

		}
		values = new String[field_names.length];
		for(int j = 0; j < field_names.length; j++) {
			values[j] = "";
		}
	}

	public int[] getField_dim() {
		return field_dim;
	}
    public int getDim_x() {
		return dim_x;
    }
}
