package microprice;

public class Tick {

    private float spread;
    private int date;
    private int timestamp;

    private float imb_bucket;
    private float next_mid;
    private float next_imb_bucket;
    private float next_spread;
    private float next_time;

    private float dM;

    public Tick(float spread, int date, int timestamp, float imb_bucket, float next_mid, float next_imb_bucket, float next_spread, float next_time, float dM, float bid, float bid_size, float ask, float ask_size, float imb, float mid, float wmid) {
        this.spread = spread;
        this.date = date;
        this.timestamp = timestamp;
        this.imb_bucket = imb_bucket;
        this.next_mid = next_mid;
        this.next_imb_bucket = next_imb_bucket;
        this.next_spread = next_spread;
        this.next_time = next_time;
        this.dM = dM;
        this.bid = bid;
        this.bid_size = bid_size;
        this.ask = ask;
        this.ask_size = ask_size;
        this.imb = imb;
        this.mid = mid;
        this.wmid = wmid;
    }

    public float getImb_bucket() {
        return imb_bucket;
    }

    public void setImb_bucket(float imb_bucket) {
        this.imb_bucket = imb_bucket;
    }

    public float getNext_mid() {
        return next_mid;
    }

    public void setNext_mid(float next_mid) {
        this.next_mid = next_mid;
    }

    public float getNext_imb_bucket() {
        return next_imb_bucket;
    }

    public void setNext_imb_bucket(float next_imb_bucket) {
        this.next_imb_bucket = next_imb_bucket;
    }

    public float getNext_spread() {
        return next_spread;
    }

    public void setNext_spread(float next_spread) {
        this.next_spread = next_spread;
    }

    public float getNext_time() {
        return next_time;
    }

    public void setNext_time(float next_time) {
        this.next_time = next_time;
    }

    public float getdM() {
        return dM;
    }

    public void setdM(float dM) {
        this.dM = dM;
    }


    public Tick(int date, int timestamp, float bid, float bid_size, float ask, float ask_size, float imb, float mid, float wmid) {
        this.date = date;
        this.timestamp = timestamp;
        this.bid = bid;
        this.bid_size = bid_size;
        this.ask = ask;
        this.ask_size = ask_size;
        this.imb = imb;
        this.mid = mid;
        this.wmid = wmid;
    }

    public Tick(String line) {

        String[] lines = line.split(",+");

        this.date = Integer.parseInt(lines[0]);
        this.timestamp = Integer.parseInt(lines[1]);

        this.bid = Float.parseFloat(lines[2]);
        this.bid_size = Float.parseFloat(lines[3]);
        this.ask = Float.parseFloat(lines[4]);
        this.ask_size = Float.parseFloat(lines[5]);

        this.imb = bid_size/(ask_size + bid_size);
        this.mid = (bid + ask)/2f;
        this.spread = ask - bid;

        this.wmid = ask*imb + bid*(1f - imb);

    }

    @Override
    public Object clone() {
        Tick user = null;
        try {
            user = (Tick) super.clone();
        } catch (CloneNotSupportedException e) {

        }

        return user;
    }



    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public float getBid() {
        return bid;
    }

    public void setBid(float bid) {
        this.bid = bid;
    }

    public float getBid_size() {
        return bid_size;
    }

    public void setBid_size(float bid_size) {
        this.bid_size = bid_size;
    }

    public float getAsk() {
        return ask;
    }

    public void setAsk(float ask) {
        this.ask = ask;
    }

    public float getAsk_size() {
        return ask_size;
    }

    public void setAsk_size(float ask_size) {
        this.ask_size = ask_size;
    }

    public float getImb() {
        return imb;
    }

    public void setImb(float imb) {
        this.imb = imb;
    }

    public float getMid() {
        return mid;
    }

    public void setMid(float mid) {
        this.mid = mid;
    }

    public float getWmid() {
        return wmid;
    }

    public void setWmid(float wmid) {
        this.wmid = wmid;
    }

    private float bid;
    private float bid_size;

    private float ask;
    private float ask_size;

    private float imb;
    private float mid;
    private float wmid;


    public int getDate() {
        return date;
    }

    public float getSpread() {
        return spread;
    }

    public void setSpread(float spread) {
        this.spread = spread;
    }

    public void setDate(int date) {
        this.date = date;
    }
}
