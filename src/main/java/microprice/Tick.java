package microprice;

public class Tick {

    private double spread;
    private int date;
    private int timestamp;

    private double imb_bucket;
    private double next_mid;
    private double next_imb_bucket;
    private double next_spread;
    private double next_time;

    private double dM;

    public Tick(double spread, int date, int timestamp, double imb_bucket, double next_mid, double next_imb_bucket, double next_spread, double next_time, double dM, double bid, double bid_size, double ask, double ask_size, double imb, double mid, double wmid) {
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

    public double getImb_bucket() {
        return imb_bucket;
    }

    public void setImb_bucket(double imb_bucket) {
        this.imb_bucket = imb_bucket;
    }

    public double getNext_mid() {
        return next_mid;
    }

    public void setNext_mid(double next_mid) {
        this.next_mid = next_mid;
    }

    public double getNext_imb_bucket() {
        return next_imb_bucket;
    }

    public void setNext_imb_bucket(double next_imb_bucket) {
        this.next_imb_bucket = next_imb_bucket;
    }

    public double getNext_spread() {
        return next_spread;
    }

    public void setNext_spread(double next_spread) {
        this.next_spread = next_spread;
    }

    public double getNext_time() {
        return next_time;
    }

    public void setNext_time(double next_time) {
        this.next_time = next_time;
    }

    public double getdM() {
        return dM;
    }

    public void setdM(double dM) {
        this.dM = dM;
    }


    public Tick(int date, int timestamp, double bid, double bid_size, double ask, double ask_size, double imb, double mid, double wmid) {
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

        this.bid = Double.parseDouble(lines[2]);
        this.bid_size = Double.parseDouble(lines[3]);
        this.ask = Double.parseDouble(lines[4]);
        this.ask_size = Double.parseDouble(lines[5]);

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

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getBid_size() {
        return bid_size;
    }

    public void setBid_size(double bid_size) {
        this.bid_size = bid_size;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getAsk_size() {
        return ask_size;
    }

    public void setAsk_size(double ask_size) {
        this.ask_size = ask_size;
    }

    public double getImb() {
        return imb;
    }

    public void setImb(double imb) {
        this.imb = imb;
    }

    public double getMid() {
        return mid;
    }

    public void setMid(double mid) {
        this.mid = mid;
    }

    public double getWmid() {
        return wmid;
    }

    public void setWmid(double wmid) {
        this.wmid = wmid;
    }

    private double bid;
    private double bid_size;

    private double ask;
    private double ask_size;

    private double imb;
    private double mid;
    private double wmid;


    public int getDate() {
        return date;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public void setDate(int date) {
        this.date = date;
    }
}
