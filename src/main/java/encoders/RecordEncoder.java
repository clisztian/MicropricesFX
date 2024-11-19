package encoders;

import encoders.TimeEncoder.Time_Encoder;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import records.AnyRecord;
import records.TimeIndicator;
import records.Type;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Encodes a record into several smaller encoders
 * 
 * 
 * @author lisztian
 *
 * @param <V>
 */
public class RecordEncoder<V>  {

	private ArrayList<V> values;
		
	private int feature_dimension;
	private Encoder[] encode_maps;
	private String[] field_names;

	/**
	 * Encoder map for AnyRecord from RecordTable
	 */
	private LinkedHashMap<String, Encoder> encoder_map;


	private AnyRecord anyRecord;



	private boolean toInt = false;


	private Type[] field_types;
	
	public RecordEncoder() {
		setValues(new ArrayList<V>());
	}
	

	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), 10);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	
	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val, int bits) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), bits);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	
	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * 
	 * If time encoder, options are
	 * 	DAY_OF_WEEK, (by default)
	    HOUR_OF_DAY,
	    DAY_OF_MONTH,
	    MONTH_OF_YEAR,
	    WEEK_OF_YEAR
	 * 
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val, int bits, boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), bits);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
				if(hours) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.HOUR_OF_DAY);
				}
				if(day_of_month) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.DAY_OF_MONTH);
				}
				if(month_of_year) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.MONTH_OF_YEAR);
				}
				if(week_of_year) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.WEEK_OF_YEAR);
				}
				
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	

	
	
	/**
	 * Instantiates a record encoder with a record containing field_names and a bit dimension
	 * @param record
	 * @param datetime_format
	 * @param dim
	 * @return
	 */
	public RecordEncoder<V> initiate(AnyRecord record, String datetime_format, int dim) {
		
		if(record.getField_names() != null) {
			
			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];
						
			for(int i = 0; i < record.getValues().length; i++) {
				
				if(field_names[i].contains("time")) {
					encode_maps[i] = new TimeEncoder("timestamp", datetime_format);
				}
				else {
					
					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], dim);
					}
					else {
						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}				
			}
			
		}
		return this;
	}
	
	


	
	/**
	 * Record encoder coming from the CSVTableView
	 * AnyRecord from CSVTableView is equipped with a Type value that is used to create the record encoder
	 * 
	 * Must include the type, and the field name
	 * If timestamps available, a datetime formatter
	 * Encodes day of week by default
	 * 
	 * @param record the model for the record data point
	 * @param datetime_format the format used for the time stamp
	 * @param dim feature dimension for real encoder
	 * @param hours encode timestamp in hours 
	 * @param day_of_month encode timestamp by day of month (good for sales data)
	 * @param month_of_year encode month of year (good for economic data)
	 * @param week_of_year encode week of the year (good for economic data)
	 * @return
	 */
	public RecordEncoder<V> initiate(AnyRecord record, String datetime_format, int dim, boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year) {
		
		
		if(record.getType() != null) {
		
			this.anyRecord = record;
			encoder_map = new LinkedHashMap<String, Encoder>();
			
			field_types = record.getType();
			field_names = record.getField_names();
						
			for(int i = 0; i < record.getType().length; i++) {
				
				if(field_types[i] == Type.TIME) {

					TimeEncoder t_encoder = new TimeEncoder(field_names[i], datetime_format);
					
					if(hours) {
						t_encoder.addTimeEncoder(Time_Encoder.HOUR_OF_DAY);
					}
					if(day_of_month) {
						t_encoder.addTimeEncoder(Time_Encoder.DAY_OF_MONTH);
					}
					if(month_of_year) {
						t_encoder.addTimeEncoder(Time_Encoder.MONTH_OF_YEAR);
					}
					if(week_of_year) {
						t_encoder.addTimeEncoder(Time_Encoder.WEEK_OF_YEAR);
					}
					encoder_map.put(field_names[i], t_encoder);
				}
				else if(field_types[i] == Type.REAL) {
					encoder_map.put(field_names[i], new RealEncoder(field_names[i], dim));
				}
				else if(field_types[i] == Type.CATEGORY) {
					encoder_map.put(field_names[i],new CategoricalEncoder(field_names[i]));
				}				
			}
			
		}
		return this;
	}
	
	
	
	//boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year)
	
	/**
	 * Instantiate a record encoder with anyRecord. Must contain field names
	 * @param record
	 * @return
	 */

	
	
	public RecordEncoder<V> initiate(AnyRecord record, int dim) {



		if(record.getField_names() != null) {
			
			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];
						
			for(int i = 0; i < record.getValues().length; i++) {
				
				if(field_names[i].contains("time") || field_names[i].contains("date")) {
					encode_maps[i] = new TimeEncoder("timestamp", "yyyy-MM-dd HH:mm:ss");
				}
				else if(field_names[i].contains("binary")) {
					encode_maps[i] = new BinaryEncoder(field_names[i]);
				}
				else {
					
					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], dim);
					}
					else {

						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}				
			}	
		}
		return this;
	}

	public RecordEncoder<V> initiateCategorical(AnyRecord record) {

		this.anyRecord = record;
		if(record.getField_names() != null) {

			field_names = record.getField_names();

			encode_maps = new Encoder[field_names.length];

			for(int i = 0; i < record.getValues().length; i++) {
				encode_maps[i] = new CategoricalEncoder(field_names[i]);
			}
		}
		return this;
	}

	public RecordEncoder<V> initiate(AnyRecord record) {

		this.anyRecord = record;
		if(record.getField_names() != null) {

			field_names = record.getField_names();

			encode_maps = new Encoder[field_names.length];

			for(int i = 0; i < record.getValues().length; i++) {

				if(field_names[i].contains("time") || field_names[i].contains("date")) {
					encode_maps[i] = new TimeEncoder("timestamp", "yyyy-MM-dd HH:mm:ss");
				}
				else {

					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], record.getField_dim()[i]);
					}
					else {
						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}
			}
		}
		return this;
	}

	public RecordEncoder<V> initiateNetworkData(AnyRecord record) {

		this.anyRecord = record;

		if(record.getField_names() != null) {

			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];

			for(int i = 0; i < record.getValues().length; i++) {
				encode_maps[i] = record.getField_dim()[i] == 1 ? new BinaryEncoder(field_names[i])
						: new RealEncoder(field_names[i], record.getField_dim()[i]);
			}

		}
		return this;
	}

	
	public void encoderTypes() {
		
		for(int i = 0; i < encode_maps.length; i++) {
			System.out.println(encode_maps[i].toString());
		}
		
	}
	
	
	/**
	 * Add a value (in terms of a record)
	 * All numerical values will be used Record
	 * @param val
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void addValue(V val) throws IllegalArgumentException, IllegalAccessException {
		
		if(val instanceof AnyRecord && encoder_map != null) {
			addRecordMapValue((AnyRecord)val);
		}		
		else if(val instanceof AnyRecord && encode_maps != null) {
			addRecordValue((AnyRecord)val);
		}
		else {			
			addClassRecord(val);
		}
	}

	private void addCategoricalData(AnyRecord any) {
		addCategoricalValues(any);
	}

	public void addCategoricalData(ArrayList<AnyRecord> records) {
		for(AnyRecord any : records) {
			addCategoricalValues(any);
		}
	}
	
	private void addRecordValue(AnyRecord any) {
		
		for(int i = 0; i < any.getValues().length; i++) {

			if(encode_maps[i] instanceof BinaryEncoder) {
				Integer value = ((Float)any.getValues()[i]).intValue();
				((BinaryEncoder)encode_maps[i]).addValue(value);
			}
			else if(encode_maps[i] instanceof RealEncoder) {
				Float value = ((Float)any.getValues()[i]);
				((RealEncoder)encode_maps[i]).addValue(value);
			}
			else if(encode_maps[i] instanceof CategoricalEncoder) {
				((CategoricalEncoder)encode_maps[i]).addValue((String)any.getValues()[i]);
			}

//			else if(any.getValues()[i] instanceof Float) {
//				Float value = (Float)any.getValues()[i];
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else if(any.getValues()[i] instanceof Double) {
//				Float value = ((Double)any.getValues()[i]).floatValue();
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else if(any.getValues()[i] instanceof Integer) {
//				Float value = ((Integer)any.getValues()[i]).floatValue();
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else if(any.getValues()[i] instanceof String) {
//				((CategoricalEncoder)encode_maps[i]).addValue((String)any.getValues()[i]);
//			}
//
//
//			if(encode_maps[i] instanceof TimeEncoder && any.getValues()[i] instanceof String) {
//				((TimeEncoder)encode_maps[i]).addValue(new Temporal((String)any.getValues()[i]));
//			}
//			else if(any.getValues()[i] instanceof Float) {
//				Float value = (Float)any.getValues()[i];
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else if(any.getValues()[i] instanceof Double) {
//				Float value = ((Double)any.getValues()[i]).floatValue();
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else if(any.getValues()[i] instanceof Integer) {
//				Float value = ((Integer)any.getValues()[i]).floatValue();
//				((RealEncoder)encode_maps[i]).addValue(value);
//			}
//			else {
//				((CategoricalEncoder)encode_maps[i]).addValue((String)any.getValues()[i]);
//			}
		}		
	}
	
	/**
	 * Add a record value from an AnyRecord map
	 * @param values
	 */
	private void addRecordMapValue(AnyRecord values) {
		
		for(int i = 0; i < anyRecord.getField_names().length; i++) {

			Encoder encode = encoder_map.get(anyRecord.getField_names()[i]);
			
			if(encode != null) {


				if(anyRecord.getType()[i] == Type.TIME && encode instanceof TimeEncoder) {
					((TimeEncoder)encode).addValue(new Temporal((String)values.getValues()[i]));
				}

				else if(anyRecord.getType()[i] == Type.REAL && encode instanceof RealEncoder) {
					Float value = (Float)values.getValues()[i];
					((RealEncoder)encode).addValue(value);
				}
				else if(anyRecord.getType()[i] == Type.CATEGORY) {
					((CategoricalEncoder)encode).addValue((String)values.getValues()[i]);
				}
			}			
		}
	}

	private void addCategoricalValues(AnyRecord values) {

		for(int i = 0; i < anyRecord.getField_names().length; i++) {

			Encoder encode = encode_maps[i];

			if(encode != null) {

				if(encode instanceof CategoricalEncoder) {
					((CategoricalEncoder)encode).addValue("" +values.getValues()[i]);
				}

			}
		}
	}

	
	
	
	private void addClassRecord(V val) throws IllegalArgumentException, IllegalAccessException {
		
		List<Field> fields = getPrivateFields(val.getClass());
		
		int count = 0;
		for(Field field : fields) {

			String type = field.getType().toString();
			if(type.contains("double")) {
							
				field.setAccessible(true);
				Float value = (float)field.getDouble(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
			}
			else if(type.contains("float")) {
				
				field.setAccessible(true);
				Float value = field.getFloat(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
				
			}
			else if(type.contains("int")) {
				
				field.setAccessible(true);
				Float value = (float)field.getInt(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
			}
			else if(type.contains("Temporal")) {
				
				field.setAccessible(true);
				Temporal value = (Temporal)field.get(val);				
				((TimeEncoder)encode_maps[count]).addValue(value);			
			}
			else { 				
				field.setAccessible(true);
				String value = field.get(val).toString();
				((CategoricalEncoder)encode_maps[count]).addValue(value);
			}	
			count++;
		}	
		
	}

	/**
	 * Fit all with the uniform rule
	 */
	public void fit_uniform() {		
		
		if(encoder_map != null) {
			for(Encoder encode : encoder_map.values()) {
				encode.fit_uniform();
			}
		}
		else {
			for(int i = 0; i < encode_maps.length; i++) {
				encode_maps[i].fit_uniform();
			}	
		}
	}
	
	/**
	 * Fit all with the dynamic rule
	 */
	public void fit_dynamic() {
		
		if(encoder_map != null) {
			for(Encoder encode : encoder_map.values()) {
				encode.fit_dynamic();
			}
		}
		else {
			for(int i = 0; i < encode_maps.length; i++) {
				encode_maps[i].fit_dynamic();
			}
		}
	}
	
	
	
	
	
	/**
	 * Transforms a record into an encoded bit representation
	 * @param val A generic record
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int[] transform(V val) throws IllegalArgumentException, IllegalAccessException {

		if(toInt) return transformForBayesian((AnyRecord)val);

		if(encoder_map != null) {
			return transform_any_map_record((AnyRecord)val);
		}
		
		if(val instanceof AnyRecord) { 		
			return transform_any_record((AnyRecord)val);
		}
			
		return transform_record(val);
	}
	
	
	/**
	 * Decodes any bit array back into their respective values
	 * @param en
	 * @return
	 */
	public ArrayList<RecordDecodeResult> decode(int[] en) {
		
		ArrayList<RecordDecodeResult> decode_list = new ArrayList<RecordDecodeResult>();
		int start = 0;
		
		if(encoder_map != null && anyRecord != null) {
			
			for(int i = 0; i < anyRecord.getField_names().length; i++) {
				
				Encoder encode = encoder_map.get(anyRecord.getField_names()[i]);
				
				if(encode != null) {
					
					int bit_dim = encode.getBitDimension();
					int[] subarray = ArrayUtils.subarray(en, start, start+bit_dim);
					start += bit_dim;
					
					RecordDecodeResult res = new RecordDecodeResult();
					res.setEncoded(subarray);
					res.setValue(encode.decoder(subarray));
					res.setField_name(anyRecord.getField_names()[i]);
					
					decode_list.add(res);
				}			
			}
						
			return decode_list;	
		}
		
		for(int i = 0; i < encode_maps.length; i++) {
			
			int bit_dim = encode_maps[i].getBitDimension();
			int[] subarray = ArrayUtils.subarray(en, start, start+bit_dim);
			start += bit_dim;
			
			RecordDecodeResult res = new RecordDecodeResult();
			res.setEncoded(subarray);
			res.setValue(encode_maps[i].decoder(subarray));
			res.setField_name(field_names[i]);
			
			decode_list.add(res);
		}				
		return decode_list;
	}
	
	
	
	/**
	 * Get number of total bits represented in record encoding 
	 * @return
	 */
	public int getBitDimension() {
		
		int dim = 0;
		
		if(encoder_map != null) {
			for(Encoder encode : encoder_map.values()) {
				dim += encode.getBitDimension();
			}
		}
		else {
			for(int i = 0; i < encode_maps.length; i++) {
				dim += encode_maps[i].getBitDimension();

				//System.out.println(field_names[i]  + " " + encode_maps[i].getBitDimension());
			}
		}
		
		return dim;	
	}

	public LinkedHashMap<Integer, String> getLiteralMap() {

		LinkedHashMap<Integer, String> literal_map = new LinkedHashMap<Integer, String>();

		int count = 0;
		for(int i = 0; i < encode_maps.length; i++) {

			int dim = encode_maps[i].getBitDimension();

			//acount for binary (two values)
			if(dim == 1) {
				dim=2;
			}

			for(int j = 0; j < dim; j++) {
				literal_map.put(count, field_names[i] + "-" + j);
				count++;
			}


			//System.out.println(field_names[i]  + " " + encode_maps[i].getBitDimension());
		}


		return literal_map;
	}
	
	
	public  List<Field> getPrivateFields(Class<?> theClass){
        LinkedList<Field> privateFields = new LinkedList<Field>();

        Field[] fields = theClass.getDeclaredFields();

        for(Field field:fields){
        	privateFields.add(field);
        }
        return privateFields;
    }


	public ArrayList<V> getValues() {
		return values;
	}


	public void setValues(ArrayList<V> values) {
		this.values = values;
	}


	/**
	 * Get the field names of the records
	 * @return
	 */
	public String[] getField_names() {
		return field_names;
	}
	
	/**
	 * Get encoder maps
	 * @return all the encoding maps
	 */
	public Encoder<V>[] getEncode_maps() {
		return encode_maps;
	}
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		Random rng = new Random();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dt = DateTime.now();
		
		String[] name_cat = new String[] {"economic", "financial", "weather", "health"};
		String[] reach_cat = new String[] {"global", "cantonal", "city", "EU"};
		
		
		RecordEncoder<TimeIndicator> rencoder = new RecordEncoder<TimeIndicator>();
		
		rencoder.initiate(TimeIndicator.class);
		
		for(int i = 0; i < 100; i++) {
			
			double value = rng.nextDouble()*10;
			int name_c = rng.nextInt(4);
			int reach_c = rng.nextInt(4);
			
			TimeIndicator ind = new TimeIndicator(name_cat[name_c], reach_cat[reach_c], new Temporal(dt.toString(formatter)), value);
			rencoder.addValue(ind);
			dt = dt.plusMinutes(120);
			
		}
		rencoder.fit_dynamic();
		
		int dim = rencoder.getBitDimension();
		
		System.out.println(dim);
		
		TimeIndicator ind = new TimeIndicator(name_cat[0], reach_cat[0], new Temporal(dt.toString(formatter)), 2);
		
		int[] enc = rencoder.transform(ind);
		
		for(int i = 0; i < enc.length; i++) {
			System.out.print(enc[i] + " ");
		} 
		System.out.println("");
		
		ArrayList<RecordDecodeResult> results = rencoder.decode(enc);
		
		for(int i = 0; i < results.size(); i++) {
			System.out.println(results.get(i));
		}
		
		
		
		/**
		 * Instantiate record encoder
		 * 
		 * public record TimeIndicator(String name, String type, Temporal timestamp, double value) {
		 * 
		 */
		RecordEncoder<AnyRecord> table_encoder = new RecordEncoder<AnyRecord>();
		
		AnyRecord any_rec = new AnyRecord();
		String[] fields = new String[5];
		fields[0] = "name";
		fields[1] = "type";
		fields[2] = "timestamp";
		fields[3] = "value";
		fields[4] = "meta";
		
		Type[] types = new Type[5];
		types[0] = Type.CATEGORY;
		types[1] = Type.CATEGORY;
		types[2] = Type.TIME;
		types[3] = Type.REAL;
		types[4] = Type.INFO;
		
		any_rec.setField_names(fields);
		any_rec.setType(types);
		
		table_encoder.initiate(any_rec, "yyyy-MM-dd HH:mm:ss", 10, false, false, false, false);
		

		for(int i = 0; i < 100; i++) {
			
			float value = rng.nextFloat()*10f;
			int name_c = rng.nextInt(4);
			int reach_c = rng.nextInt(4);
			
			AnyRecord rec = new AnyRecord();
			
			Object[] vals = new Object[] {name_cat[name_c], reach_cat[reach_c], dt.toString(formatter), value, name_c};
			rec.setValues(vals);
			
			
			table_encoder.addValue(rec);
			dt = dt.plusMinutes(120);
			
		}
		

//		for(Encoder enco : table_encoder.getEncoder_map().values()) {
//			
//			System.out.println(enco.getClass());
//			if(enco instanceof RealEncoder) {
//				System.out.println(((RealEncoder)enco).getValues().size());
//			}
//		}
		
		table_encoder.fit_dynamic();
		
		System.out.println("After fit dynamic");
		
		AnyRecord an = new AnyRecord();
		an.setValues(new Object[] {name_cat[0], reach_cat[0], dt.toString(formatter), 2f, name_cat[0]});
		
		int[] enc1 = table_encoder.transform(an);
		
		for(int i = 0; i < enc1.length; i++) {
			System.out.print(enc1[i] + " ");
		} 
		System.out.println("");
		
		ArrayList<RecordDecodeResult> newresults = table_encoder.decode(enc1);
		
		for(int i = 0; i < newresults.size(); i++) {
			System.out.println(newresults.get(i));
		}
		
	}


	public int getFeature_dimension() {
		return feature_dimension;
	}


	public void setFeature_dimension(int feature_dimension) {
		this.feature_dimension = feature_dimension;
	}
	

	/**
	 * Transforms any generic record
	 * @param any
	 * @return
	 */
	private int[] transform_any_record(AnyRecord any) {
		
		int[] encoded = null;

		for(int i = 0; i < any.getValues().length; i++) {

			if(encode_maps[i] instanceof BinaryEncoder) {
				encoded = ArrayUtils.addAll(encoded, ((Float)any.getValues()[i]).intValue());
			}
			else if(encode_maps[i] instanceof TimeEncoder && any.getValues()[i] instanceof String) {
				int[] bits = ((TimeEncoder)encode_maps[i]).transform(new Temporal((String)any.getValues()[i]));
				encoded = ArrayUtils.addAll(encoded, bits);	
			}
			else if(encode_maps[i] instanceof CategoricalEncoder) {
				int[] bits = ((CategoricalEncoder)encode_maps[i]).transform((String)any.getValues()[i]);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(encode_maps[i] instanceof RealEncoder) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform((Float)any.getValues()[i]);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(any.getValues()[i] instanceof Float) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform((Float)any.getValues()[i]);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(any.getValues()[i] instanceof Double) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform(((Double)any.getValues()[i]).floatValue());
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(any.getValues()[i] instanceof Integer) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform(((Integer)any.getValues()[i]).floatValue());
				encoded = ArrayUtils.addAll(encoded, bits);
			}

		}

		return encoded;		
	}

	private int[] transformForBayesian(AnyRecord any) {

		int[] encoded = null;

		for(int i = 0; i < any.getValues().length; i++) {

			if(encode_maps[i] instanceof BinaryEncoder) {
//				int[] bits = new int[1];
//				bits[0] = ((Float)any.getValues()[i]).intValue();
//				System.out.println("BinaryEncoder: " + bits.length);
				encoded = ArrayUtils.addAll(encoded, ((Float)any.getValues()[i]).intValue());
			}
			else if(encode_maps[i] instanceof TimeEncoder && any.getValues()[i] instanceof String) {
				//System.out.println("should not be here");
				int[] bits = ((TimeEncoder)encode_maps[i]).transform(new Temporal((String)any.getValues()[i]));
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(encode_maps[i] instanceof CategoricalEncoder) {

				int bits = ((CategoricalEncoder)encode_maps[i]).transformToInt((String)any.getValues()[i]);
				//System.out.println("CategoryEncoder: " + bits);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(encode_maps[i] instanceof RealEncoder) {
				int val = ((RealEncoder)encode_maps[i]).transformToInt((Float)any.getValues()[i]) - 1;
				//System.out.println("RealEncoder: " + val);
				encoded = ArrayUtils.addAll(encoded, val);
			}
			else if(any.getValues()[i] instanceof Float) {
				//System.out.println("should not be here");
				encoded = ArrayUtils.addAll(encoded, ((RealEncoder)encode_maps[i]).transformToInt((Float)any.getValues()[i]));
			}
			else if(any.getValues()[i] instanceof Double) {
				//System.out.println("should not be here");
				encoded = ArrayUtils.addAll(encoded, ((RealEncoder)encode_maps[i]).transformToInt((Float)any.getValues()[i]));
			}
			else if(any.getValues()[i] instanceof Integer) {
				//System.out.println("should not be here");
				encoded = ArrayUtils.addAll(encoded, ((RealEncoder)encode_maps[i]).transformToInt((Float)any.getValues()[i]));
			}

		}

		return encoded;
	}

	/**
	 * Transform any record using the encoder map
	 * @param values
	 * @return
	 */
	private int[] transform_any_map_record(AnyRecord values) {
		
		int[] encoded = null;
		
		for(int i = 0; i < anyRecord.getField_names().length; i++) {
			
			Encoder encode = encoder_map.get(anyRecord.getField_names()[i]);
			
			if(encode != null) {
				
				if(anyRecord.getType()[i] == Type.TIME && encode instanceof TimeEncoder) {

					Temporal temp = new Temporal((String)values.getValues()[i]);
					int[] bits = ((TimeEncoder)encode).transform(temp);
					encoded = ArrayUtils.addAll(encoded, bits);	
				}
				else if(anyRecord.getType()[i] == Type.REAL && encode instanceof RealEncoder) {
					
					Float value = (Float)values.getValues()[i];
					int[] bits = ((RealEncoder)encode).transform(value);
					encoded = ArrayUtils.addAll(encoded, bits);
				}
				else if(anyRecord.getType()[i] == Type.CATEGORY) {
					int[] bits = ((CategoricalEncoder)encode).transform(values.getValues()[i].toString());
					encoded = ArrayUtils.addAll(encoded, bits);
				}
			}			
		}
		return encoded;		
		
	}
	
	
	
	/**
	 * Transforms a predefined record
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private int[] transform_record(V val) throws IllegalArgumentException, IllegalAccessException {
		
		List<Field> fields = getPrivateFields(val.getClass());
		
		int count = 0;
		int[] encoded = null;
		
		for(Field field : fields) {

			String type = field.getType().toString();
			if(type.contains("double")) {
							
				field.setAccessible(true);
				Float value = (float)field.getDouble(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("float")) {
				
				field.setAccessible(true);
				Float value = field.getFloat(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("int")) {
				
				field.setAccessible(true);
				Float value = (float)field.getInt(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("Temporal")) {
				
				field.setAccessible(true);
				Temporal value = (Temporal)field.get(val);				
				int[] bits = ((TimeEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);	
			}
			else { 				
				field.setAccessible(true);
				String value = field.get(val).toString();
				int[] bits = ((CategoricalEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}	
			count++;
		}					
		return encoded;	

	}

	public Type[] getField_types() {
		return field_types;
	}
	public LinkedHashMap<String, Encoder> getEncoder_map() {
		return encoder_map;
	}


	public void setEncoder_map(LinkedHashMap<String, Encoder> encoder_map) {
		this.encoder_map = encoder_map;
	}


	public AnyRecord getAnyRecord() {
		return anyRecord;
	}


	public void setAnyRecord(AnyRecord anyRecord) {
		this.anyRecord = anyRecord;
	}

	public boolean isToInt() {
		return toInt;
	}

	public void setToInt(boolean toInt) {
		this.toInt = toInt;
	}
	
	
	
	
	
}
