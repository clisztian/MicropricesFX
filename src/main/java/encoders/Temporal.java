package encoders;

import encoders.TimeEncoder.TimeEncoderResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * Time instantiates a representation of a time, where a DateTime object will be used to track 
 * encoding into different features
 * @author lisztian
 *
 */
public class Temporal {
	

	private DateTimeFormatter date_formatter;
	private String date_time_format;
	private String date_time_string;
	private TimeEncoderResult[] time_results;

	/**
	 * A Time object with onlt the decoded time results
	 * @param results
	 */
	public Temporal(TimeEncoderResult[] results) {
		time_results = results;
	}
	
	public Temporal(String date_time_string) {
		this.date_time_string = date_time_string;
	}
	
	
	public String getDate_time_string() {
		return date_time_string;
	}

	public void setDate_time_string(String date_time) {
		this.date_time_string = date_time;
	}

	public String getDate_time_format() {
		return date_time_format;
	}

	public void setDate_time_format(String date_time_format) {
		this.date_time_format = date_time_format;
	}

	public DateTimeFormatter getDate_formatter() {
		return date_formatter;
	}

	/**
	 * Set the date time formater
	 * @param date_formatter
	 */
	public void setDate_formatter(DateTimeFormatter date_formatter) {
		this.date_formatter = date_formatter;
	}
	
	/**
	 * Get the DateTime representation of this time object
	 * @return
	 */
	public DateTime getDateTime() {
		return date_formatter.parseDateTime(date_time_string);
	}


	public TimeEncoderResult[] getTime_results() {
		return time_results;
	}


	public void setTime_results(TimeEncoderResult[] time_results) {
		this.time_results = time_results;
	}
	
}