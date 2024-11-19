package encoders;

import java.util.ArrayList;

public interface Encoder<V> {

	/**
	 * Adds a new value to the in-sample list
	 * @param value
	 */
	public void addValue(V value);
	public void fit_uniform();
	public void fit_dynamic();	
	public void fit_dynamic(ArrayList<V> list);
	public int getBitDimension();
	public int[] transform(V value);
	public V decoder(int[] enc);
}
