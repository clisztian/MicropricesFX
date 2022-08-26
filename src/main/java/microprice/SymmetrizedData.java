package microprice;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.math.Quantiles;
import util.MutableInt;

public class SymmetrizedData {

    private final ArrayList<Double> quants;
    private int n_spread;

    public ArrayList<Double> getQuants() {
        return quants;
    }

    private ArrayList<Tick> d;
    private int dt;
    private double ticksize;

    private int n_imb;

    public int getN_spread() {
        return n_spread;
    }

    public void setN_spread(int n_spread) {
        this.n_spread = n_spread;
    }

    public ArrayList<Tick> getD() {
        return d;
    }

    public void setD(ArrayList<Tick> d) {
        this.d = d;
    }

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public double getTicksize() {
        return ticksize;
    }

    public void setTicksize(Double ticksize) {
        this.ticksize = ticksize;
    }

    public int getN_imb() {
        return n_imb;
    }

    public void setN_imb(int n_imb) {
        this.n_imb = n_imb;
    }

    public HashMap<Double, MutableInt> getSpreads() {
        return spreads;
    }

    public void setSpreads(HashMap<Double, MutableInt> spreads) {
        this.spreads = spreads;
    }

    private HashMap<Double, MutableInt> spreads;
    private ArrayList<Double> all_spreads;

    private ArrayList<Double> all_dms;

    public ArrayList<Double> getAll_dms() {
        return all_dms;
    }

    public void setAll_dms(ArrayList<Double> all_dms) {
        this.all_dms = all_dms;
    }

    public HashMap<Double, MutableInt> getMid() {
        return mid;
    }

    public void setMid(HashMap<Double, MutableInt> mid) {
        this.mid = mid;
    }

    private HashMap<Double, MutableInt> mid;


    private Table<Double, Double, ArrayList<Tick>> next_imb_bucket_map_no_move;
    private Table<Double, Double, ArrayList<Tick>> next_imb_bucket_map;

    private Table<Double, Double, ArrayList<Tick>> spread_imb_bucket_map;

    public Table<Double, Double, ArrayList<Tick>> getSpread_imb_bucket_map() {
        return spread_imb_bucket_map;
    }

    public void setSpread_imb_bucket_map(Table<Double, Double, ArrayList<Tick>> spread_imb_bucket_map) {
        this.spread_imb_bucket_map = spread_imb_bucket_map;
    }

    public Table<Double, Double, ArrayList<Tick>> getNext_imb_bucket_map_no_move() {
        return next_imb_bucket_map_no_move;
    }

    public void setNext_imb_bucket_map_no_move(Table<Double, Double, ArrayList<Tick>> next_imb_bucket_map_no_move) {
        this.next_imb_bucket_map_no_move = next_imb_bucket_map_no_move;
    }

    public Table<Double, Double, ArrayList<Tick>> getNext_imb_bucket_map() {
        return next_imb_bucket_map;
    }

    public void setNext_imb_bucket_map(Table<Double, Double, ArrayList<Tick>> next_imb_bucket_map) {
        this.next_imb_bucket_map = next_imb_bucket_map;
    }

    public ArrayList<Double> getAll_spreads() {
        return all_spreads;
    }

    public void setAll_spreads(ArrayList<Double> all_spreads) {
        this.all_spreads = all_spreads;
    }

    public SymmetrizedData(ArrayList<Tick> d, int dt, int n_spread) {

        spreads = new HashMap<Double, MutableInt>();
        mid = new HashMap<Double, MutableInt>();

        this.n_imb = 10;
        this.dt = dt;
        this.n_spread = n_spread;
        this.ticksize = d.stream().filter(t -> t.getSpread() > 0)
                .min(Comparator.comparingDouble(Tick::getSpread)).get().getSpread();

        this.ticksize = Math.round(this.ticksize*100.0)/100.0;

        System.out.println("total size before filter: " + d.size());
        for(int i = 0; i < 80; i++) {

            Tick t = d.get(i);
            System.out.println(i + " " + t.getDate() + " " + t.getTimestamp() + " " + t.getSpread() + " " + n_spread*ticksize);

        }



        d = d.stream().filter(t-> (t.getSpread() > 0 && t.getSpread() <= n_spread*ticksize))
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("total size after spread filter: " + d.size());

        List<Double> imbs = d.stream().map(tick -> tick.getImb())
                .collect(Collectors.toList());

        List<Integer> buckets = new ArrayList<>();
        for(int i = 1; i <= 10; i++) buckets.add(i*10);

        Map<Integer, Double> qcut = Quantiles.percentiles().indexes(buckets).compute(imbs);

        quants = new ArrayList<Double>();
        for(Map.Entry<Integer, Double> e : qcut.entrySet()) {
            System.out.println(e.getValue() + " " + e.getKey());
            quants.add(e.getValue());

        }
        Collections.sort(quants);



        next_imb_bucket_map_no_move = HashBasedTable.create();
        next_imb_bucket_map = HashBasedTable.create();
        spread_imb_bucket_map = HashBasedTable.create();


        this.d = new ArrayList<Tick>();



        ArrayList<Tick> mirror = new ArrayList<>();
        for(int i = 0; i < d.size() - dt; i++) {

            Tick t = d.get(i);
            Tick next_t = d.get(i+dt);

            /**
             * format to 4 decimals
             */
            t.setSpread( Math.round((t.getSpread())/this.ticksize) * this.ticksize  );
            next_t.setSpread( Math.round((next_t.getSpread())/this.ticksize) * this.ticksize  );

            t.setImb_bucket( _qcuit(t.getImb(), quants, 10.0));

            t.setNext_imb_bucket( _qcuit(next_t.getImb(), quants, 10.0));
            t.setNext_mid( next_t.getMid());
            t.setNext_spread(next_t.getSpread());
            t.setNext_time(next_t.getTimestamp());

            t.setdM( Math.round ( (next_t.getMid() - t.getMid()) / (this.ticksize*2.0)) * (this.ticksize/2.0)  );




            if( Math.abs(t.getdM()) <= ticksize*1.1f  ) {

                this.d.add(t);

                MutableInt m = spreads.get(t.getSpread());
                if(m == null) {
                    m = new MutableInt(); spreads.put(t.getSpread(), m);
                }
                else {
                    m.increment();
                }

                MutableInt dm = mid.get(t.getdM());
                if(dm == null) {
                    dm = new MutableInt(); mid.put(t.getdM(), dm);
                }
                else {
                    dm.increment();
                }


                Tick copy_t = new Tick(t.getSpread(),
                        t.getDate(),
                        t.getTimestamp(),
                        t.getImb_bucket(),
                        t.getNext_mid(),
                        t.getNext_imb_bucket(),
                        t.getNext_spread(),
                        t.getNext_time(),
                        t.getdM(),
                        t.getBid(),
                        t.getBid(),
                        t.getAsk(),
                        t.getAsk_size(),
                        t.getImb(),
                        t.getMid(),
                        t.getWmid());

                copy_t.setdM(-t.getdM());
                copy_t.setMid(-t.getMid());

                copy_t.setImb_bucket(9 - t.getImb_bucket());
                copy_t.setNext_imb_bucket(9 - t.getNext_imb_bucket());

                mirror.add(copy_t);

                if(t.getdM() == 0) {

                    ArrayList<Tick> nmlist = next_imb_bucket_map_no_move.get(t.getSpread(), t.getNext_imb_bucket());
                    if(nmlist == null) {
                        nmlist = new ArrayList<Tick>(); nmlist.add(t);
                        next_imb_bucket_map_no_move.put(t.getSpread(), t.getNext_imb_bucket(), nmlist);

                    }
                    else {
                        nmlist.add(t);

                    }



                    ArrayList<Tick> nmlist2 = next_imb_bucket_map_no_move.get(t.getSpread(), copy_t.getNext_imb_bucket());
                    if(nmlist2 == null) {
                        nmlist2 = new ArrayList<Tick>();  nmlist2.add(copy_t);
                        next_imb_bucket_map_no_move.put(t.getSpread(), copy_t.getNext_imb_bucket(), nmlist2);
                    }
                    else {
                        nmlist2.add(copy_t);
                    }


                }
                else {

                    ArrayList<Tick> nmlist = next_imb_bucket_map.get(t.getSpread(), t.getdM());
                    if(nmlist == null) {
                        nmlist = new ArrayList<>(); nmlist.add(t);
                        next_imb_bucket_map.put(t.getSpread(), t.getdM(), nmlist);
                    }
                    else {
                        nmlist.add(t);
                    }

                    ArrayList<Tick> nmlist2 = next_imb_bucket_map.get(t.getSpread(), copy_t.getdM());
                    if(nmlist2 == null) {
                        nmlist2 = new ArrayList<>(); nmlist2.add(copy_t);
                        next_imb_bucket_map.put(t.getSpread(), copy_t.getdM(), nmlist2);
                    }
                    else {
                        nmlist2.add(copy_t);
                    }



                    ArrayList<Tick> list = spread_imb_bucket_map.get(t.getSpread(), t.getImb_bucket());
                    if(list == null) {
                        list = new ArrayList<Tick>(); list.add(t);
                        spread_imb_bucket_map.put(t.getSpread(), t.getNext_imb_bucket(), list);

                    }
                    else {
                        list.add(t);

                    }



                    ArrayList<Tick> list2 = spread_imb_bucket_map.get(t.getSpread(), copy_t.getImb_bucket());
                    if(list2 == null) {
                        list2 = new ArrayList<Tick>();  list2.add(copy_t);
                        spread_imb_bucket_map.put(t.getSpread(), copy_t.getNext_imb_bucket(), list2);
                    }
                    else {
                        list2.add(copy_t);
                    }


                }

            }
        }


        all_spreads = new ArrayList<>();
        all_dms = new ArrayList<>();
        all_dms.addAll(mid.keySet());
        all_spreads.addAll(spreads.keySet());

//        for(Map.Entry<Double, MutableInt> e : spreads.entrySet()) {
//
//            all_spreads.add(e.getKey());
//
//            for(int i = 0; i < 10; i++) {
//                if(next_imb_bucket_map_no_move.get(e.getKey(), (Double)i) != null) {
//                    System.out.println(i + " " + next_imb_bucket_map_no_move.get(e.getKey(), (Double)i).size());
//                }
//            }
//
//            System.out.println("diff map");
//            for(Map.Entry<Double, MutableInt> me : mid.entrySet()) {
//
//                if(next_imb_bucket_map.get(e.getKey(), (me.getKey())) != null) {
//                    System.out.println(me.getKey() + " " + next_imb_bucket_map.get(e.getKey(), me.getKey()).size());
//                }
//            }
//            System.out.println();
//        }
        Collections.sort(all_spreads);
        Collections.sort(all_dms);

        System.out.println();
        for(Double dms : all_dms) {
            System.out.println(dms);
        }

        System.out.println("total rows: " + this.d.size() + " " + mirror.size());
        this.d.addAll(mirror);



        for(int i = 0; i < 30; i++) {

            Tick t = this.d.get(i);
            System.out.println(i + " " + t.getDate() + " " + t.getTimestamp() + " " + t.getImb() + " " + t.getImb_bucket() + " " + t.getNext_imb_bucket() + " " + t.getSpread() + " " + t.getdM());

        }

        for(int i = this.d.size() - 30; i < this.d.size(); i++) {

            Tick t = this.d.get(i);
            System.out.println(i + " " + t.getDate() + " " + t.getTimestamp() + " " + t.getImb() + " " + t.getImb_bucket() + " " + t.getNext_imb_bucket() + " " + t.getSpread() + " " + t.getdM());

        }

        System.out.println("total rows: " + this.d.size());

    }

    public static Double _qcuit(Double imb, ArrayList<Double> bucket, Double size) {

        for(int i = 0; i < bucket.size(); i++) {
            if(imb > bucket.get(i)) {

            }
            else {
                return i*1.0;
            }
        }
        return bucket.size()*1.0;
    }

    public static void main(String[] args) throws IOException {

        ArrayList<Tick> ticks = DataStore.getTickData("/home/lisztian/MicropriceFX/data/bac.csv");

        SymmetrizedData sym = new SymmetrizedData(ticks, 1, 2);

    }

}