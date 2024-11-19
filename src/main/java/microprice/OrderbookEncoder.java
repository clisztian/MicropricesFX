package microprice;


import hyperdimension.encoders.IntervalEmbedding;
import hyperdimension.encoders.SparseIntervalEmbedding;
import hyperdimension.encoders.VanillaBHV;
import hyperdimension.sparse.SparseBinaryVector;

import java.util.ArrayList;
import java.util.List;

public class OrderbookEncoder {

    private final static double SLACK = .00001;

    private int spreadSizeMax = 50;
    private int priceChangeMax = 50;


    //price percent embedding, 10 intervals between 0 and 1, .10, .20, .30, .40, .50, .60, .70, .80, .90, 1.0
    private SparseIntervalEmbedding pricePercentEmbedding;
    private SparseIntervalEmbedding spreadSizeEmbedding;
    private SparseIntervalEmbedding priceChangeEmbedding;
    private SparseIntervalEmbedding volumeEmbedding;
    private SparseIntervalEmbedding micropriceEmbedding;

    private IntervalEmbedding micropriceIntervalEmbedding;

    private IntervalEmbedding volumeIntervalEmbedding;
    private IntervalEmbedding priceChangeIntervalEmbedding;

    private SparseIntervalEmbedding targetEmbedding;


    public OrderbookEncoder() {

    }

    public void createSpreadEmbedding(double min, double max) {
        spreadSizeEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createPriceChangeEmbedding(double min, double max) {
        priceChangeEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createVolumeEmbedding(double min, double max) {
        volumeEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createMicropriceEmbedding(double min, double max) {
        micropriceEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createPricePercentEmbedding(double min, double max) {
        pricePercentEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createTargetEmbedding(double min, double max) {
        targetEmbedding = new SparseIntervalEmbedding(min-SLACK, max+SLACK, 3);
    }

    public void createMicropriceIntervalEmbedding(double min, double max) {
        micropriceIntervalEmbedding = new IntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createVolumeIntervalEmbedding(double min, double max) {
        volumeIntervalEmbedding = new IntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public void createPriceChangeIntervalEmbedding(double min, double max) {
        priceChangeIntervalEmbedding = new IntervalEmbedding(min-SLACK, max+SLACK, 10);
    }

    public VanillaBHV ecnodeDense(Microprice microprice) {

        VanillaBHV vector = micropriceIntervalEmbedding.forward(microprice.getAdjustedMicroprice());

        VanillaBHV volumeVector = volumeIntervalEmbedding.forward(microprice.getLastXTrades());

        VanillaBHV priceChangeVector = priceChangeIntervalEmbedding.forward(microprice.getPriceChange());

        return vector.xor(volumeVector).xor(priceChangeVector);
    }

    public VanillaBHV encodeDense(Microprice microprice, VanillaBHV target) {

        VanillaBHV vector = micropriceIntervalEmbedding.forward(microprice.getAdjustedMicroprice());

        VanillaBHV volumeVector = volumeIntervalEmbedding.forward(microprice.getLastXTrades());

        VanillaBHV priceChangeVector = priceChangeIntervalEmbedding.forward(microprice.getPriceChange());

        //put the vectors into a List
        List<VanillaBHV> vectors = new ArrayList<>();
        vectors.add(vector);
        vectors.add(volumeVector);
        vectors.add(priceChangeVector);

        //compute the majority of the vectors
        VanillaBHV majority = VanillaBHV.logic_majority(vectors);
        return majority.xor(target);
    }

    public SparseBinaryVector encode(Microprice microprice) {

        //first encode all the bid and ask ranks
        List<SparseBinaryVector> vectors = new ArrayList<>();
        vectors.add(pricePercentEmbedding.forward(microprice.getAskRank1()));
        vectors.add(pricePercentEmbedding.forward(microprice.getAskRank2()).permute(1));
        vectors.add(pricePercentEmbedding.forward(microprice.getAskRank3()).permute(2));
        vectors.add(pricePercentEmbedding.forward(microprice.getAskRank4()).permute(3));
        vectors.add(pricePercentEmbedding.forward(microprice.getAskRank5()).permute(4));
        vectors.add(pricePercentEmbedding.forward(microprice.getBidRank1()));
        vectors.add(pricePercentEmbedding.forward(microprice.getBidRank2()).permute(1));
        vectors.add(pricePercentEmbedding.forward(microprice.getBidRank3()).permute(2));
        vectors.add(pricePercentEmbedding.forward(microprice.getBidRank4()).permute(3));
        vectors.add(pricePercentEmbedding.forward(microprice.getBidRank5()).permute(4));

        //encode the spread size
        vectors.add(spreadSizeEmbedding.forward(microprice.getSpreadSize()).permute(5));

        //encode the price change
        vectors.add(priceChangeEmbedding.forward(microprice.getPriceChange()).permute(6));

        //encode the last x trades
        vectors.add(volumeEmbedding.forward(microprice.getLastXTrades()).permute(7));

        //encode the microprice
        vectors.add(micropriceEmbedding.forward(microprice.getAdjustedMicroprice()).permute(8));


        if(microprice.getPriceChange() > 0) {
            return micropriceEmbedding.forward(1.0);
        } else if(microprice.getPriceChange() < 0) {
            return micropriceEmbedding.forward(2.0);
        } else {
            return micropriceEmbedding.forward(0.0);
        }


        //now bundle all the vectors into one
        //return micropriceEmbedding.forward(microprice.getAdjustedMicroprice());
    }




}