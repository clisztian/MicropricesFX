package dynamics;

import encoders.RecordEncoder;
import records.AnyRecord;
import series.TimeSeries;
import tsetlin.ConvolutionEncoder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Interface from records to input for clause learning
 * 
 * Includes a record encoder and a hierarchical encoder to map from 
 * 
 * record sequence -> bit sequence -> convolution encoder for convolutional learning
 * 
 * Sample length is the minimum in-sample sequence length for training
 * Window is the time-dependent patch size on which learning is achieved
 * 
 * @author lisztian
 *
 */
public class Evolutionize<V> {

	private int window;
	private int sample_length;

	private ArrayList<int[]> encoded_records;

	private RecordEncoder<V> encoder;
	private ConvolutionEncoder conv_encoder;
	private int dim_x;
	private Histories historical;

	
	/**
	 * Evolutionize instantiates a learning framework using a window and a lag_length
	 * 
	 * @param window total size of the learning period
	 * @param sample_length
	 */
	public Evolutionize(int window, int sample_length) {
		
		this.window = window;
		this.sample_length = sample_length;

		encoded_records = new ArrayList<int[]>();
		encoder = new RecordEncoder<V>();		
	}

	public Evolutionize(int window, int sample_length, boolean toInt) {

		this.window = window;
		this.sample_length = sample_length;

		encoded_records = new ArrayList<int[]>();
		encoder = new RecordEncoder<V>();
		encoder.setToInt(toInt);
	}
	
	/**
	 * Initiates an evolutionize for AnyRecord, given a datetime_format and parameters used for encoding the datetime
	 * @param record
	 * @param datetime_format
	 * @param bits
	 * @param hours
	 * @param day_of_month
	 * @param month_of_year
	 * @param week_of_year
	 */
	public void initiate(AnyRecord record, String datetime_format, int bits, boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year) {	
		encoder.initiate(record, datetime_format, bits, hours, day_of_month, month_of_year, week_of_year);
	}
	
	
	/**
	 * Initiate the evolutionizer with any record, a datetime format and real encoder dimension
	 * @param record
	 * @param datetime_format
	 * @param dim
	 */
	public void initiate(AnyRecord record, String datetime_format, int dim) {		
		encoder.initiate(record, datetime_format, dim);		
	}
	
	/**
	 * Initiate the internal encoder with an anyrecord
	 * @param record
	 */
	public void initiate(AnyRecord record) {
		encoder.initiate(record);
	}
	public void initiateCategorical(AnyRecord record) {
		encoder.initiateCategorical(record);
	}

	public void initiate(AnyRecord record, int bits) {
		encoder.initiate(record, bits);
	}
	
	public void initiate(Class<?> val) {		
		encoder.initiate(val);
	}
	
	public void initiate(Class<?> val, int bits) {	
		encoder.initiate(val, bits);
	}
	
	public void initiate(Class<?> val, int bits, boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year) {	
		encoder.initiate(val, bits, hours, day_of_month, month_of_year, week_of_year);
	}
	

	public LinkedHashMap<Integer, String> getLiteralMap() {
		return encoder.getLiteralMap();
	}
	
	public void addValue(V val) throws IllegalArgumentException, IllegalAccessException {		
		encoder.addValue(val);
	}
	
	public void fit() {
		encoder.fit_uniform();
	}
	
	public void initiateConvolutionEncoder() {
		
		dim_x = encoder.getBitDimension();
		conv_encoder = new ConvolutionEncoder(dim_x, sample_length, window);
		historical = new Histories(sample_length, dim_x);
	}
	
	

	

		
	public TimeSeries<int[]> encode(ArrayList<V> records) throws IllegalArgumentException, IllegalAccessException {
		
		TimeSeries<int[]> series = new TimeSeries<int[]>();
		
		for(int i = 0; i < records.size(); i++) {
			series.add(""+i, encoder.transform(records.get(i)));;	
		}
			
		return series;	
	}
	

	/**
	 * Adds a new value and updates the historical data to reflect new observation value
	 * Returns the latest sample ready to be inputed into covolutional model
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int[] encode_add(V val) throws IllegalArgumentException, IllegalAccessException {
		
		int[] encoded_val = encoder.transform(val);
		encoded_records.add(encoded_val);		
		historical.addHistory(encoded_val);
		
		return conv_encoder.bit_encode(flatten(historical.getHistories()));			
	}
	
	
	/**
	 * Simple addition of new sample to history
	 * @param val
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void add(V val) throws IllegalArgumentException, IllegalAccessException {		
		historical.addHistory(encoder.transform(val));	
	}
	

	public int[] transformToBit(V val) throws IllegalArgumentException, IllegalAccessException {
		return encoder.transform(val);
	}



	public int[] get_last_sample() {	
		return conv_encoder.bit_encode(flatten(historical.getHistories()));		
	}
	
	
	public int getWindow() {
		return window;
	}

	public void setWindow(int window_lag) {
		this.window = window_lag;
	}

	public RecordEncoder<V> getEncoder() {
		return encoder;
	}

	public void setEncoder(RecordEncoder<V> encoder) {
		this.encoder = encoder;
	}

	public ConvolutionEncoder getConv_encoder() {
		return conv_encoder;
	}

	public void setConv_encoder(ConvolutionEncoder conv_encoder) {
		this.conv_encoder = conv_encoder;
	}

	public Histories getHistorical() {
		return historical;
	}

	public void setHistorical(Histories historical) {
		this.historical = historical;
	}
	
	private static int[] flatten(int[][] data) {
	    return Stream.of(data)
                .flatMapToInt(IntStream::of)
                .toArray();
	}
	
	public int getEncoderDimension() {
		return dim_x;
	}


	public void initiateData(ArrayList<AnyRecord> records) {
		encoder.addCategoricalData(records);
	}

	public void initiateNetworkData(AnyRecord dataModel) {
		encoder.initiateNetworkData(dataModel);
	}
}
