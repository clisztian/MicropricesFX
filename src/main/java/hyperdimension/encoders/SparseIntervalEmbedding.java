package hyperdimension.encoders;

import hyperdimension.sparse.SparseBinaryVector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class SparseIntervalEmbedding {

    private List<SparseBinaryVector> intervals;
    private double low;
    private double high;
    private double step;

    private double SLACK = .001;
    public SparseIntervalEmbedding(double low, double high, int divisions) {
        this.low = low - SLACK;
        this.high = high + SLACK;
        this.step = (this.high - this.low) / divisions;
        this.intervals = new ArrayList<>();
        for (int i = 0; i < divisions; i++) {
            this.intervals.add(SparseBinaryVector.rand());
        }
    }

    public SparseBinaryVector forward(double x) {
        int index = (int) ((x - low) / step);
        return intervals.get(index);
    }

    public double back(SparseBinaryVector hv) {
        int index = IntStream.range(0, intervals.size())
                .boxed()
                .min(Comparator.comparingInt(i -> hv.hammingDistance(intervals.get(i))))
                .orElse(0);
        return low + index * step;
    }

}
