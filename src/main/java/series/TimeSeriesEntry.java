package series;



/**
 * The time series entry with generic value V and
 * a string as the timeStamp which is typically in the form
 * of a standard DataTimeFormatter, for example
 * "yyyy-MM-dd HH:mm:ss"
 * "dd-MM-yyyy" 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 * @param <V>
 */


public class TimeSeriesEntry<V> {

	private final String timeStamp;

    public String getTimeStamp() {
        return timeStamp;
    }

    public V getValue() {
        return value;
    }

    private final V value;

    public TimeSeriesEntry(String timeStamp, V value) {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public String getDateTime() {
		return timeStamp;
	}
}