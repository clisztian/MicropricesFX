package records;

public class OrderbookSignalUpdateRecord {

	public double time_diff;
	public float spread;
	public float log_price;
	public int volume;
	public float bid_vol;
	public float ask_vol;
	public float sig_1;
	public float sig_2;
	public float sig_3;

	public OrderbookSignalUpdateRecord(double time_diff, float spread, float log_price, int volume, float bid_vol, float ask_vol, float sig_1, float sig_2, float sig_3) {
		this.time_diff = time_diff;
		this.spread = spread;
		this.log_price = log_price;
		this.volume = volume;
		this.bid_vol = bid_vol;
		this.ask_vol = ask_vol;
		this.sig_1 = sig_1;
		this.sig_2 = sig_2;
		this.sig_3 = sig_3;
	}
}
