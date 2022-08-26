package microprice;
import com.google.common.collect.Table;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Estimate {

    private  RealMatrix B;
    private  RealMatrix G1;
    DecimalFormat df;
    DecimalFormat df1;

    private LinkedHashMap<Double, Double[]> microprices;

    private SymmetrizedData data;

    public Estimate(SymmetrizedData data) {
        df = new DecimalFormat("0.00000");
        df1 = new DecimalFormat("#.#");
        this.data = data;
    }

    public void createStateMatrix() {



        int n_imb = data.getN_imb();
        int n_imb2 = data.getQuants().size()*data.getAll_spreads().size();
        RealMatrix Q1 = MatrixUtils.createRealMatrix(n_imb2, n_imb2);
        RealMatrix Q = MatrixUtils.createRealMatrix(n_imb2, n_imb2);
        RealMatrix Q2 = MatrixUtils.createRealMatrix(n_imb2, n_imb2);
        RealMatrix R2 = MatrixUtils.createRealMatrix(n_imb2, n_imb2);
        RealMatrix R1 = MatrixUtils.createRealMatrix(n_imb2, data.getAll_dms().size()-1);


        Table<Double, Double, ArrayList<Tick>> next_no_move = data.getNext_imb_bucket_map_no_move();
        Table<Double, Double, ArrayList<Tick>> next_move = data.getNext_imb_bucket_map();
        Table<Double, Double, ArrayList<Tick>> spread_move = data.getSpread_imb_bucket_map();



        for(int i = 0; i < data.getAll_spreads().size(); i++) {

            Double s = data.getAll_spreads().get(i);
            //build Q block diagnonal matrix
            for(int j = 0; j < n_imb; j++) {


                ArrayList<Tick> ticks = next_no_move.get(s, (double)j);
                int[] counts = new int[n_imb];

                if(ticks != null) {
                    for(Tick t : ticks) {
                        counts[(int)t.getImb_bucket()]++;
                    }
                }

                for(int k = 0; k < n_imb; k++) Q1.setEntry(i * data.getN_imb() + j, i * data.getN_imb() + k, counts[k]);

            }

            int row_count = 0;
            for(Double dm : data.getAll_dms()) {

                if(dm != 0) {

                    ArrayList<Tick> ticks2 = next_move.get(s, dm);
                    int[] counts2 = new int[n_imb];

                    if(ticks2 != null) {
                        for(Tick t : ticks2) {
                            counts2[(int)t.getImb_bucket()]++;
                        }
                    }

                    for(int k = 0; k < n_imb; k++) R1.setEntry(i * data.getN_imb()  + k, row_count,  counts2[k]);

                    row_count++;
                }
            }


            for(int j = 0; j < n_imb; j++) {


                ArrayList<Tick> ticks3 = spread_move.get(s, (double)j);

                if(ticks3 != null) {

                    for(int sp = 0; sp < data.getAll_spreads().size(); sp++) {

                        Double ns = data.getAll_spreads().get(sp);
                        List<Tick> ns_ticks = ticks3.stream().filter(tick -> tick.getNext_spread() == ns).collect(Collectors.toList());

                        int[] counts = new int[n_imb];

                        if(ns_ticks != null) {
                            for(Tick t : ns_ticks) {
                                counts[(int)t.getNext_imb_bucket()]++;
                            }
                        }

                        for(int k = 0; k < n_imb; k++) R2.setEntry(i * data.getN_imb() + j, sp * data.getN_imb() + k, counts[k]);
                    }
                }
            }
        }

        System.out.println("Q counts");

        for(int i = 0; i < Q1.getRowDimension(); i++) {

            for(int j = 0; j < Q1.getColumnDimension(); j++) {
                System.out.print(df1.format(Q1.getEntry(i,j)) + ", ");
            }
            System.out.println();
        }

        System.out.println("sum: " + Q1.getRowVector(0).getL1Norm());



        for(int r = 0; r < n_imb2; r++) {

            double sum_row = Q1.getRowVector(r).getL1Norm() + R1.getRowVector(r).getL1Norm();
            double sum_row2 = Q1.getRowVector(r).getL1Norm() + R2.getRowVector(r).getL1Norm();

            if(sum_row > 0) {
                Q.setRowVector(r, Q1.getRowVector(r).mapMultiply(1.0/sum_row));
                R1.setRowVector(r, R1.getRowVector(r).mapMultiply(1.0/sum_row));
            }

            if(sum_row2 > 0) {
                Q2.setRowVector(r, Q1.getRowVector(r).mapMultiply(1.0/sum_row2));
                R2.setRowVector(r, R2.getRowVector(r).mapMultiply(1.0/sum_row2));

            }

        }

        System.out.println();
        for(int i = 0; i < Q.getRowDimension(); i++) {

            for(int j = 0; j < Q.getColumnDimension(); j++) {
                System.out.print(df.format(Q.getEntry(i,j)) + ", ");
            }
            System.out.println();
        }

        System.out.println();


//        for(int i = 0; i < R2.getRowDimension(); i++) {
//
//            for(int j = 0; j < R2.getColumnDimension(); j++) {
//                System.out.print(df.format(R2.getEntry(i,j)) + ", ");
//            }
//            System.out.println();
//        }


        Double[] mids = data.getAll_dms().stream().filter(aDouble -> aDouble != 0).collect(Collectors.toList()).toArray(new Double[0]);

        System.out.println("K");
        double[] vec_all = new double[mids.length];
        for(int i = 0; i < mids.length; i++) {
            vec_all[i] = mids[i];
            System.out.println(vec_all[i]);
        }

        RealMatrix K = MatrixUtils.createRealMatrix(vec_all.length,1);
        K.setColumn(0, vec_all);

        /**
         * Create G1
         */
        RealMatrix eye = MatrixUtils.createRealIdentityMatrix(n_imb2);
        RealMatrix inverseEyeQ = MatrixUtils.inverse(eye.subtract(Q));

        G1 = (inverseEyeQ.multiply(R1)).multiply(K);

//        for(int i = 0; i < G1.getRowDimension(); i++) {
//
//            for(int j = 0; j < G1.getColumnDimension(); j++) {
//                System.out.print(df.format(G1.getEntry(i,j)) + ", ");
//            }
//            System.out.println();
//        }

        B = inverseEyeQ.multiply(R2);

//        System.out.println("Print B");
//        for(int i = 0; i < B.getRowDimension(); i++) {
//
//            for(int j = 0; j < B.getColumnDimension(); j++) {
//                System.out.print(df.format(B.getEntry(i,j)) + ", ");
//            }
//            System.out.println();
//        }
//        System.out.println();
    }

    public void recursive(int n_iterations) {

        RealMatrix G2 = B.multiply(G1).add(G1);
        RealMatrix G3 = B.power(2).multiply(G1).add(G2);
        RealMatrix G4 = B.power(3).multiply(G1).add(G3);
        RealMatrix G5 = B.power(4).multiply(G1).add(G4);
        RealMatrix G6 = B.power(5).multiply(G1).add(G5);

        microprices = new LinkedHashMap<Double, Double[]>();

        int count_spread = 0;
        for(Double s : data.getAll_spreads()) {

            Double[] adj = new Double[data.getN_imb()];
            for (int j = 0; j < data.getN_imb(); j++) {
                adj[j] = (Double) G6.getEntry(count_spread * data.getN_imb() + j, 0);
            }
            microprices.put(s, adj);

            count_spread++;

            System.out.println("Spread: " + s + ": " + toStringImb(microprices.get(s)));
        }

    }


    public String toStringImb(Double[] imb) {

        String s = new String();

        for(int i = 0; i < imb.length-1; i++) {
            s += df.format(imb[i]) + ", ";
        }
        s += df.format(imb[imb.length-1]);

        return s;
    }

    public static void main(String[] args) throws IOException {

        ArrayList<Tick> ticks = DataStore.getTickData("/home/lisztian/AutomataFX/workspace/MicropricesFX/data/bac.csv");

        SymmetrizedData sym = new SymmetrizedData(ticks, 1, 2);


        Estimate est = new Estimate(sym);

        est.createStateMatrix();

        est.recursive(5);


    }

}
