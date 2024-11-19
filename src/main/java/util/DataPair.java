package util;

public class DataPair {

    public DataPair(float[][] d, int[] l) {
        this.data = d;
        this.labels = l;
    }

    public float[][] getData() {
        return data;
    }
    public void setData(float[][] data) {
        this.data = data;
    }
    public int[] getLabels() {
        return labels;
    }
    public void setLabels(int[] labels) {
        this.labels = labels;
    }
    private float[][] data;
    private int[] labels;

}