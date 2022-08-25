package microprice;
import com.google.common.collect.Table;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Estimate {

    private SymmetrizedData data;

    public Estimate(SymmetrizedData data) {
        this.data = data;
    }

    public void createStateMatrix() {

        int n_imb = data.getN_imb();
        int n_imb2 = data.getQuants().size()*data.getAll_spreads().size();
        RealMatrix no_move_count = MatrixUtils.createRealMatrix(n_imb2, n_imb2);
        RealMatrix move_count = MatrixUtils.createRealMatrix(n_imb2, data.getAll_dms().size()-1);

        Table<Float, Float, ArrayList<Tick>> next_no_move = data.getNext_imb_bucket_map_no_move();
        Table<Float, Float, ArrayList<Tick>> next_move = data.getNext_imb_bucket_map();

        for(int i = 0; i < data.getAll_spreads().size(); i++) {

            float s = data.getAll_spreads().get(i);
            //build Q block diagnonal matrix
            for(int j = 0; j < n_imb; j++) {


                ArrayList<Tick> ticks = next_no_move.get(s, (float)j);
                int[] counts = new int[n_imb];

                if(ticks != null) {
                    for(Tick t : ticks) {
                        counts[(int)t.getImb_bucket()]++;
                    }
                }

                for(int k = 0; k < n_imb; k++) no_move_count.setEntry(i * data.getN_imb() + j, i * data.getN_imb() + k, counts[k]);

            }

            int row_count = 0;
            for(Float dm : data.getAll_dms()) {

                if(dm != 0) {

                    ArrayList<Tick> ticks2 = next_move.get(s, dm);
                    int[] counts2 = new int[n_imb];

                    if(ticks2 != null) {
                        for(Tick t : ticks2) {
                            counts2[(int)t.getImb_bucket()]++;
                        }
                    }

                    for(int k = 0; k < n_imb; k++) move_count.setEntry(i * data.getN_imb()  + k, row_count,  counts2[k]);

                    row_count++;
                }
            }

        }

        for(int r = 0; r < n_imb2; r++) {

            double sum_row = no_move_count.getRowVector(r).getL1Norm() + move_count.getRowVector(r).getL1Norm();

            no_move_count.setRowVector(r, no_move_count.getRowVector(r).mapMultiply(1.0/sum_row));
            move_count.setRowVector(r, move_count.getRowVector(r).mapMultiply(1.0/sum_row));
        }


        System.out.println(move_count.toString());

    }

    public static void main(String[] args) throws IOException {

        ArrayList<Tick> ticks = DataStore.getTickData("/home/lisztian/MicropriceFX/data/bac.csv");

        SymmetrizedData sym = new SymmetrizedData(ticks, 4, 20);


        Estimate est = new Estimate(sym);

        est.createStateMatrix();

    }

}
