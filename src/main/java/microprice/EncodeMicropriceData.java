package microprice;

import hyperdimension.encoders.IntervalEmbedding;
import hyperdimension.encoders.VanillaBHV;
import hyperdimension.sparse.SparseBinaryVector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import tsetlin.AutomataLearning;
import tsetlin.ConvolutionEncoder;
import util.HVC;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static microprice.MicropriceReader.readMicroprices;

public class EncodeMicropriceData {


    private final int inSample;
    private final int outSample;
    private OrderbookSequenceEncoder orderbookSequenceEncoder;
    private List<Microprice> micropriceData;
    private OrderbookEncoder orderbookEncoder;
    private List<SparseBinaryVector> hv_vectors = new ArrayList<>();

    private List<VanillaBHV> vanillaBHVS = new ArrayList<>();
    private IntervalEmbedding pricePercentEmbedding;
    private List<Integer> targets = new ArrayList<>();
    private int[][] X_train;
    private int[] y_train;

    private int[][] X_test;

    private int[] y_test;

    ConvolutionEncoder convolutionEncoder = new ConvolutionEncoder(HVC.DIMENSION, 1, 1);

    private final Map<VanillaBHV, Number> associativeMemory = new HashMap<>();

    int windowLength = 2;

    public EncodeMicropriceData(String fileName) {

        Random random = new Random(System.currentTimeMillis());

        micropriceData = readMicroprices(fileName);
        orderbookEncoder = new OrderbookEncoder();

        orderbookSequenceEncoder = new OrderbookSequenceEncoder(windowLength);

        //get the min and max values for each feature
        DescriptiveStatistics adjustedMicropriceStats = new DescriptiveStatistics();
        DescriptiveStatistics priceChange = new DescriptiveStatistics();
        DescriptiveStatistics spread = new DescriptiveStatistics();

        for (Microprice microprice : micropriceData) {
            adjustedMicropriceStats.addValue(microprice.getAdjustedMicroprice());
            priceChange.addValue(microprice.getPriceChange());
            spread.addValue(microprice.getSpreadSize());
        }

        orderbookEncoder.createPricePercentEmbedding(0, .65);
        orderbookEncoder.createSpreadEmbedding(0, 10);
        orderbookEncoder.createPriceChangeEmbedding(priceChange.getMin()*.50, priceChange.getMax()*.50);
        orderbookEncoder.createMicropriceEmbedding(adjustedMicropriceStats.getMin()*.70, adjustedMicropriceStats.getMax()*.70);
        orderbookEncoder.createVolumeEmbedding(0, 1);
        orderbookEncoder.createTargetEmbedding(0, 2);
        orderbookEncoder.createMicropriceIntervalEmbedding(adjustedMicropriceStats.getMin()*.70, adjustedMicropriceStats.getMax()*.70);
        orderbookEncoder.createVolumeIntervalEmbedding(0, 1);
        orderbookEncoder.createPriceChangeIntervalEmbedding(priceChange.getMin()*.50, priceChange.getMax()*.50);

        pricePercentEmbedding = new IntervalEmbedding(0, 2, 3);

        for(int i = 0; i < micropriceData.size()-1; i++) {

            Microprice m = micropriceData.get(i);
            hv_vectors.add(orderbookEncoder.encode(m));
            if (m.getPriceChange() > 0) {
                targets.add(1);
                vanillaBHVS.add(pricePercentEmbedding.forward(1).xor(orderbookEncoder.ecnodeDense(m)));
            } else if (m.getPriceChange() < 0) {
                targets.add(2);
                vanillaBHVS.add(pricePercentEmbedding.forward(2).xor(orderbookEncoder.ecnodeDense(m)));
            } else {
                targets.add(0);
                vanillaBHVS.add(pricePercentEmbedding.forward(0).xor(orderbookEncoder.ecnodeDense(m)));
            }
        }



        inSample = (int)(micropriceData.size() * .7);
        outSample = micropriceData.size() - inSample;

        for(int i = windowLength; i < inSample; i++) {

            Microprice m = micropriceData.get(i);
            Microprice m_prev = micropriceData.get(i-1);
            Microprice m_next = micropriceData.get(i + 1);

            VanillaBHV target;
            if(m.getPriceChange() > 0) {
                target = pricePercentEmbedding.forward(1);
            } else if(m.getPriceChange() < 0) {
                target = pricePercentEmbedding.forward(2);
            } else {
                target = random.nextDouble() < .5 ? pricePercentEmbedding.forward(1) : pricePercentEmbedding.forward(2);
            }


            //combine m and m_prev into a list
            List<VanillaBHV> sequence = new ArrayList<>();
            //add the three previous Microprice objects to the sequence list
            for(int k = 0; k < windowLength; k++) {
                sequence.add(orderbookEncoder.encodeDense(micropriceData.get(i - k), target));
            }

            VanillaBHV encoded = orderbookSequenceEncoder.encodeSequence(sequence);


            if(m_next.getPriceChange() > 0) {
                associativeMemory.put(VanillaBHV.logic_majority(List.of(pricePercentEmbedding.forward(1), encoded)), 1);
            } else if(m_next.getPriceChange() < 0) {
                associativeMemory.put(VanillaBHV.logic_majority(List.of(pricePercentEmbedding.forward(2), encoded)), 2);
            }
        }

        //now with the rest of the data, predict the next price change using the associative memory
        for(int i = inSample; i < micropriceData.size()-1; i++) {

            Microprice m = micropriceData.get(i);
            Microprice m_next = micropriceData.get(i + 1);

            VanillaBHV target;
            if(m.getPriceChange() > 0) {
                target = pricePercentEmbedding.forward(1);
            } else if(m.getPriceChange() < 0) {
                target = pricePercentEmbedding.forward(2);
            } else {
                target = random.nextDouble() < .5 ?  pricePercentEmbedding.forward(1) : pricePercentEmbedding.forward(2);
            }

            int actual = 0;
            if(m_next.getPriceChange() > 0) {
                actual = 1;
            } else if(m_next.getPriceChange() < 0) {
                actual = 2;
            } else {
                actual = random.nextDouble() < .5 ? 1 : 2;
            }


            //combine m and m_prev into a list
            //combine m and m_prev into a list
            List<VanillaBHV> sequence = new ArrayList<>();
            //add the three previous Microprice objects to the sequence list
            for(int k = 0; k < windowLength; k++) {
                sequence.add(orderbookEncoder.encodeDense(micropriceData.get(i - k), target));
            }

            VanillaBHV encoded = orderbookSequenceEncoder.encodeSequence(sequence);

            //with the encoded, make three VanillaBHV objects, one for each target
            VanillaBHV target1 = VanillaBHV.logic_majority(List.of(pricePercentEmbedding.forward(1), encoded));
            VanillaBHV target2 = VanillaBHV.logic_majority(List.of(pricePercentEmbedding.forward(2), encoded));

            //for each target, compute the predictNextElement
            Pair<Number, Integer> prediction1 = predictNextElement(target1);
            Pair<Number, Integer> prediction2 = predictNextElement(target2);
            //Pair<Number, Integer> prediction0 = predictNextElement(target0);

            int minDistance = Math.min(prediction1.getRight(), prediction2.getRight());
            Number bestMatch = -1;
            if(minDistance == prediction1.getRight()) {
                bestMatch = prediction1.getLeft();
            } else if(minDistance == prediction2.getRight()) {
                bestMatch = prediction2.getLeft();
            }
        }
    }

    public Pair<Number, Integer> predictNextElement(VanillaBHV queryVector) {
        Number bestMatch = -1;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<VanillaBHV, Number> entry : associativeMemory.entrySet()) {
            int distance =  queryVector.hammingDistance(entry.getKey());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = entry.getValue();
            }
        }
        //return the best match and the distance
        return Pair.of(bestMatch, minDistance);
    }



    //create data for training
    private void createEncodedData(int windowSize) {

        int[][] X_encoder = new int[hv_vectors.size() - windowSize-1][];
        int[] y_encoder = new int[hv_vectors.size() - windowSize-1];

        for(int i = windowSize; i < hv_vectors.size()-1; i++) {

            //take the last windowSize vectors and encode them
            List<VanillaBHV> sequence = new ArrayList<>();
            for(int j = 0; j < windowSize; j++) {
                sequence.add(vanillaBHVS.get(i - j + 1));
            }

            VanillaBHV encoded = orderbookSequenceEncoder.encodeSequence(sequence);

            //encode the sequence
            //SparseBinaryVector encoded = encodeSequence(sequence);
            int[] encodedX = convolutionEncoder.bit_encode(encoded.toBooleanIntArray());

            X_encoder[i - windowSize] = encodedX;
            //after the first 100 orderbooks, the label is 1
            y_encoder[i- windowSize] = targets.get(i + 1);
            //System.out.println("Encoded: " + y_encoder[i - windowSize]);

        }
        createTrainingData(X_encoder, y_encoder);

    }

    public SparseBinaryVector encodeSequence(List<SparseBinaryVector> sequence) {

        //permite and then bind the vectors
        SparseBinaryVector nGramVector = SparseBinaryVector.zeroVector();
        for(int i = 0; i < sequence.size(); i++) {

            //permute the vector
            SparseBinaryVector permuted = sequence.get(i).permute(i);
            nGramVector = nGramVector.bind(permuted);
        }
        return nGramVector;
    }


    private void createTrainingData(int[][] X_encoder, int[] y_encoder) {


        Random random = new Random();
        //take 80 percent of the data for training
        int trainSize = (int)(X_encoder.length * .8);
        //build a random set of unique indices from 1 - X.length
        int[] indices = new int[X_encoder.length];
        for(int i = 0; i < X_encoder.length; i++) {
            indices[i] = i;
        }
        //shuffle the indices
        for(int i = 0; i < X_encoder.length; i++) {
            int j = (int)(random.nextDouble() * X_encoder.length);
            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }

        X_train = new int[trainSize][];
        y_train = new int[trainSize];
        for(int i = 0; i < trainSize; i++) {
            X_train[i] = X_encoder[indices[i]];
            y_train[i] = y_encoder[indices[i]];
        }

        X_test = new int[X_encoder.length - trainSize][];
        y_test = new int[X_encoder.length - trainSize];

        for(int i = trainSize; i < X_encoder.length; i++) {
            X_test[i - trainSize] = X_encoder[indices[i]];
            y_test[i - trainSize] = y_encoder[indices[i]];
        }
    }

    public void learnTMRandom(int numberSimulations) {

        Random rng = new Random(System.currentTimeMillis());

        for(int n = 0; n < numberSimulations; n++) {

            int numberClauses = 200 + rng.nextInt(200);
            int threshold =20 + rng.nextInt(100);
            float spec =        10f + 10f*rng.nextFloat();
            int numberLiterals = rng.nextInt(200);

            AutomataLearning model = new AutomataLearning(
                    convolutionEncoder,
                    numberClauses,
                    threshold ,
                    spec,
                    3,
                    .1f);

            model.setMaxNumberOfLiterals(numberLiterals);
            model.setNegativeFocusedSampling(false);

            for (int e = 0; e < 1; e++) {

                model.fit(X_train, y_train);

                //compute the accuracy of each class
                int[] classPredicted = new int[3];
                int[] classCorrect = new int[3];
                //compute the accuracy
                int correct = 0;
                for (int i = 0; i < X_test.length; i++) {
                    int pred = model.predict(X_test[i]);
                    if(pred == y_test[i]) {
                        correct++;
                        classCorrect[pred]++;
                    }
                    classPredicted[pred]++;
                }
            }

        }
    }



    public static void main(String[] args) {

        EncodeMicropriceData encoder = new EncodeMicropriceData("microprice_tsla.csv");
        encoder.createEncodedData(2);
        encoder.learnTMRandom(10);

    }

}
