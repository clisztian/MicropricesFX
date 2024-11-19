package series;


import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * The underlying TimeSeries class for all the time series types.
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 * @param <V>
 */
public class TimeSeries<V> extends ArrayList<TimeSeriesEntry<V>> {
	
	private static final long serialVersionUID = 4071035106419033490L;

    @SuppressWarnings("rawtypes")
    public static final TimeSeries EMPTY_SERIES = new TimeSeries<>(0);
	
	public TimeSeries() {
		super();
	}

	public TimeSeries(Collection<? extends TimeSeriesEntry<V>> c) {
		super(c);
	}

	public TimeSeries(int initialCapacity) {
		super(initialCapacity);
	}
	
	public void add(String timeStamp, V value) {
		add(new TimeSeriesEntry<V>(timeStamp, value));
	}

    @SuppressWarnings("unchecked")
    public static final <T> TimeSeries<T> empty() {
        return EMPTY_SERIES;
    }
    
    public TimeSeriesEntry<V> last() {
        return get(size() - 1);
    }

}