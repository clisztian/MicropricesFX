package encoders;

import java.util.ArrayList;

public class BinaryEncoder implements Encoder<Integer> {

    private final String feature_name;
    private final int max_bits;
    private final int number_of_features;

    public BinaryEncoder(String name) {
        this.feature_name = name;
        this.max_bits = 1;

        values = new ArrayList<Integer>();
        number_of_features = 1;
    }

    private ArrayList<Integer> values;
    @Override
    public void addValue(Integer value) {
        values.add(value);
    }

    @Override
    public void fit_uniform() {

    }

    @Override
    public void fit_dynamic() {

    }

    @Override
    public void fit_dynamic(ArrayList<Integer> list) {

    }

    @Override
    public int getBitDimension() {
        return 1;
    }

    @Override
    public int[] transform(Integer value) {

        return new int[] {value};
    }

    @Override
    public Integer decoder(int[] enc) {
        return enc[0];
    }
}
