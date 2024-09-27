package hyperdimension.examples;

import com.github.signaflo.timeseries.TimeSeries;
import hyperdimension.encoders.VanillaBHV;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static util.Util.sampleMAModel;
import static util.Util.sampleSeasonalARModel;

public class TimeSeriesPrediction {

    private final int nSeries = 100;
    private final int seriesLength = 216;

    private final int numberGroups = 2;

    private int nGrams = 1;
    private int quantization = 100;

    DecimalFormat df = new DecimalFormat("#.##");
    final List<double[]> seriesAll = new ArrayList<>();
    //when simulating entire new data sets
    public void simulateData() {

        //simulates many different types of sequences

        long timestart = System.currentTimeMillis();
        Random random = new Random();
        //simulate 100 sequences of length 144
        seriesAll.clear();

        int[] labels = new int[2 * nSeries];

        for (int i = 0; i < 2*nSeries; i++) {
            if (i < nSeries) {

                //randomly select a phase for the harmonic model
                int phase = 1+ random.nextInt(3);
                TimeSeries series = sampleMAModel(seriesLength);
                //sample a cyclic harmonic model
                double[] data = new double[seriesLength];
                for (int j = 0; j < seriesLength; j++) {
                    data[j] =  4*Math.cos(2 * Math.PI * j / 12 + phase * Math.PI / 3);
                }


                seriesAll.add(data);
                labels[i] = 0;
            } else {

                TimeSeries series = sampleSeasonalARModel(seriesLength);
                double[] data = new double[seriesLength];
                for (int j = 0; j < seriesLength; j++) {
                    data[j] = series.at(j);
                }

                seriesAll.add(data);
                labels[i] = 1;
            }
        }

        //now transform seriesAll into a double[][] array
        double[][] data = new double[seriesAll.size()][seriesLength];

        for (int i = 0; i < seriesAll.size(); i++) {
            for (int j = 0; j < seriesLength; j++) {
                data[i][j] = seriesAll.get(i)[j];
//                //add a cosine wave to the series
//                if (i >= nSeries) {
//                    data[i][j] += Math.cos(2 * Math.PI * j / 12);
//                }
            }
        }

        //instantiate a SequenceEncoder object
        final SequenceEncoder encoder = new SequenceEncoder(data,  quantization, nGrams);
        List<VanillaBHV> encodedSequences = encoder.getEncodedSequences();

        //aggregate the encoded sequences into two groups, one for the first nSeries and the other for the second nSeries
        List<VanillaBHV> group1 = new ArrayList<>();
        List<VanillaBHV> group2 = new ArrayList<>();
        for (int i = 0; i < 2 * nSeries; i++) {
            if (i < nSeries) {
                group1.add(encodedSequences.get(i));
            } else {
                group2.add(encodedSequences.get(i));
            }
        }




        VanillaBHV bundle1 = VanillaBHV.logic_majority(group1);
        VanillaBHV bundle2 = VanillaBHV.logic_majority(group2);

        int total = 100; //total number of classifications
        int count = 0; //count the number of correct classifications
        for (int i = 0; i < total; i++) {
            //sample a sequence from label
            TimeSeries sample = sampleSeasonalARModel(seriesLength);
            double[] sampleData = new double[seriesLength];
            for (int j = 0; j < seriesLength; j++) {
                int phase = 1+ random.nextInt(3);
                sampleData[j] = 4*Math.cos(2 * Math.PI * j / 12 + phase * Math.PI / 3);
            }
            VanillaBHV sampleEncoded = encoder.encodeSequence(sampleData);


            double distance1 = sampleEncoded.hammingDistance(bundle1);
            double distance2 = sampleEncoded.hammingDistance(bundle2);

            //print the distances
            System.out.println("Distance to bundle1: " + df.format(distance1) + ", Distance to bundle2: " + df.format(distance2));

            //in general should be closer to bundle2
            if (distance1 < distance2) {
                count++;
            }
        }

        System.out.println("Accuracy: " + (count * 100.0 / total) + "%");



    }

    public static void main(String[] args) {
        TimeSeriesPrediction tsp = new TimeSeriesPrediction();
        tsp.simulateData();
    }

}
