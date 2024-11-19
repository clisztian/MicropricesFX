package encoders;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * TimeEncoder encodes a datetime stamp into various bit encoded sequences
 * @author lisztian
 *
 */
public class TimeEncoder implements Encoder<Temporal> {

	public enum Time_Encoder {
	    DAY_OF_WEEK,
	    HOUR_OF_DAY,
	    DAY_OF_MONTH,
	    MONTH_OF_YEAR,
	    WEEK_OF_YEAR
	}
	
	private String feature_name;
	private DateTimeFormatter date_formatter;
	private String date_time_format;
	private String date_time_string;
	private ArrayList<Temporal> values;
	private ArrayList<Time_Encoder> time_encoders;
	
	/**
	 * Instantiates a time encoder with the default time encoding of day of week
	 * @param date_time_format
	 */
	public TimeEncoder(String date_time_format) {
		
		values = new ArrayList<Temporal>();
		time_encoders = new ArrayList<Time_Encoder>();
		date_formatter = DateTimeFormat.forPattern(date_time_format);
		time_encoders.add(Time_Encoder.DAY_OF_WEEK);
	}
	
	/**
	 * Instantiates a time encoder with the default time encoding of day of week
	 * @param date_time_format
	 */
	public TimeEncoder(String name, String date_time_format) {
		
		feature_name = name;
		values = new ArrayList<Temporal>();
		time_encoders = new ArrayList<Time_Encoder>();
		date_formatter = DateTimeFormat.forPattern(date_time_format);
		time_encoders.add(Time_Encoder.DAY_OF_WEEK);
	}
	
	
	
	public void addTimeEncoder(Time_Encoder enc) {
		time_encoders.add(enc);
	}
	

	@Override
	public void addValue(Temporal value) {
		values.add(value);
	}
	
	@Override
	public void fit_uniform() {
		
	}
	@Override
	public void fit_dynamic() {
		
	}
	@Override
	public void fit_dynamic(ArrayList<Temporal> list) {
		
	}
	
	@Override
	public int[] transform(Temporal value) {
		
		
		int[] time_encoded = null;
		DateTime time = getDateTime(value);
		
		for(Time_Encoder enc : time_encoders) {
			
			int[] bits = null;
			if(enc.equals(Time_Encoder.DAY_OF_WEEK)) {
				bits = new int[7];
				int day = time.getDayOfWeek();
				for(int i = 0; i < day; i++) bits[i] = 1;
			}
			else if(enc.equals(Time_Encoder.HOUR_OF_DAY)) {
				bits = new int[24];
				int hour = time.getHourOfDay();
				for(int i = 0; i < hour; i++) bits[i] = 1;
			}
			else if(enc.equals(Time_Encoder.DAY_OF_MONTH)) {
				bits = new int[31];
				int day = time.getDayOfMonth();
				for(int i = 0; i < day; i++) bits[i] = 1;
			}
			else if(enc.equals(Time_Encoder.MONTH_OF_YEAR)) {
				bits = new int[12];
				int day = time.getMonthOfYear();
				for(int i = 0; i < day; i++) bits[i] = 1;
			}
			else if(enc.equals(Time_Encoder.WEEK_OF_YEAR)) {
				bits = new int[52];
				int week = time.getWeekOfWeekyear();
				for(int i = 0; i < week; i++) bits[i] = 1;
			}
			
			time_encoded = ArrayUtils.addAll(time_encoded,bits);
		}	
		return time_encoded;
	}
	
	
	public DateTime getDateTime(Temporal value) {
		return date_formatter.parseDateTime(value.getDate_time_string());
	}

	
	
	public DateTimeFormatter getDate_formatter() {
		return date_formatter;
	}
	public void setDate_formatter(DateTimeFormatter date_formatter) {
		this.date_formatter = date_formatter;
	}
	public String getDate_time_format() {
		return date_time_format;
	}
	public void setDate_time_format(String date_time_format) {
		this.date_time_format = date_time_format;
	}
	public String getDate_time_string() {
		return date_time_string;
	}
	public void setDate_time_string(String date_time_string) {
		this.date_time_string = date_time_string;
	}
	public ArrayList<Temporal> getValues() {
		return values;
	}
	public void setValues(ArrayList<Temporal> values) {
		this.values = values;
	}


	public ArrayList<Time_Encoder> getTime_encoders() {
		return time_encoders;
	}


	public void setTime_encoders(ArrayList<Time_Encoder> time_encoders) {
		this.time_encoders = time_encoders;
	}


	@Override
	public int getBitDimension() {
		
		int dim = 0;
		for(Time_Encoder enc : time_encoders) {
			
			if(enc.equals(Time_Encoder.DAY_OF_WEEK)) {
				dim += 7;
			}
			else if(enc.equals(Time_Encoder.HOUR_OF_DAY)) {
				dim += 24;
			}
			else if(enc.equals(Time_Encoder.DAY_OF_MONTH)) {
				dim += 31;
			}
			else if(enc.equals(Time_Encoder.MONTH_OF_YEAR)) {
				dim += 12;
			}
			else if(enc.equals(Time_Encoder.WEEK_OF_YEAR)) {
				dim += 52;
			}		
		}			
		return dim;	
	}
	
	/**
	 * Returns the dimensions for the time encoded bit array
	 * Example: DAY_OF_WEEK, HOUR_OF_DAY
	 * Split: 7,31
	 * @return
	 */
	public int[] getSplit_dimensions() {
		
		int[] split_dimensions = new int[time_encoders.size()];
		
		int dim = 0; int count = 0;
		for(Time_Encoder enc : time_encoders) {
			
			if(enc.equals(Time_Encoder.DAY_OF_WEEK)) {
				dim += 7;
				split_dimensions[count] = dim; 
			}
			else if(enc.equals(Time_Encoder.HOUR_OF_DAY)) {
				dim += 24;
				split_dimensions[count] = dim; 
			}
			else if(enc.equals(Time_Encoder.DAY_OF_MONTH)) {
				dim += 31;
				split_dimensions[count] = dim; 
			}
			else if(enc.equals(Time_Encoder.MONTH_OF_YEAR)) {
				dim += 12;
				split_dimensions[count] = dim; 
			}
			else if(enc.equals(Time_Encoder.WEEK_OF_YEAR)) {
				dim += 52;
				split_dimensions[count] = dim; 
			}
			count++;
		}		
		return split_dimensions;			
	}
	
	
	public TimeEncoderResult[] decode(int[] en) {
		
		int[] dims = getSplit_dimensions();
		TimeEncoderResult[] decoded = new TimeEncoderResult[dims.length];
		
		int start = 0; 
		for(int k = 0; k < dims.length; k++) {
			
			int val = 0;
			int[] subset = new int[dims[k] - start];
			for(int i = start; i < dims[k]; i++) {
				if(en[i] == 0) {
					val = i - start; break;
				}
				subset[i - start] = 1;
			}
			start = dims[k];
			
			decoded[k] = new TimeEncoderResult(val, time_encoders.get(k), subset);				
		}
		
		
		return decoded;
	}
	
	
	public static void main(String[] args) {
		
		TimeEncoder encoder = new TimeEncoder("yyyy-MM-dd HH:mm:ss");
		
		encoder.addTimeEncoder(Time_Encoder.HOUR_OF_DAY);
		encoder.addTimeEncoder(Time_Encoder.DAY_OF_MONTH);
		
		
		int[] time_enc = encoder.transform(new Temporal("2020-11-10 20:35:32"));
		
		int dim = encoder.getBitDimension();
		
		int[] dims = encoder.getSplit_dimensions();
		
		System.out.println("Dimension: " + dim);
		
		for(int i = 0; i < dims.length; i++) {
			System.out.print(dims[i] + " ");
		}
		
		for(int i = 0; i < dim; i++) {
			System.out.print(time_enc[i] + " ");
		}
		
		TimeEncoderResult[] decoded = encoder.decode(time_enc);
		System.out.println("\n");
		
		for(int i = 0; i < decoded.length; i++) {
			System.out.println(decoded[i].toString());
		}
		
	}
	
	public String[] getTemporalNames() {
		String[] names = new String[time_encoders.size()];
		for(int i = 0; i < time_encoders.size(); i++) {
			names[i] = time_encoders.get(i).toString();
		}
		return names;
	}
	
	
	public String getFeature_name() {
		return feature_name;
	}

	public void setFeature_name(String feature_name) {
		this.feature_name = feature_name;
	}


	class TimeEncoderResult {
		
		private int value;
		private Time_Encoder time_encoder;
		private int[] encoded;
		
		public TimeEncoderResult(int value, Time_Encoder time_encoder, int[] encoded) {
			
			this.value = value;
			this.time_encoder = time_encoder;
			this.encoded = encoded;
		}
		
		public int[] getEncoded() {
			return encoded;
		}
		
		public void setEncoded(int[] encoded) {
			this.encoded = encoded;
		}
		
		public Time_Encoder getTime_encoder() {
			return time_encoder;
		}
		
		public void setTime_encoder(Time_Encoder time_encoder) {
			this.time_encoder = time_encoder;
		}
		
		public int getValue() {
			return value;
		}
		
		public void setValue(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			
			String myString = "Value: " + value + "\n";
			myString += "TimeEncoder: " + time_encoder + "\n";
			
			for(int i = 0; i < encoded.length; i++) {
				myString += encoded[i] + " ";
			}
			myString += "\n";
			return myString;
		}
	}


	/**
	 * Decodes a bit array into various time components
	 */
	@Override
	public Temporal decoder(int[] enc) {	
		return new Temporal(decode(enc));
	}
	
}





