package hyperdimension.sequences;

import hyperdimension.encoders.SparseIntervalEmbedding;
import hyperdimension.sparse.SparseBinaryVector;
import ucr.TimeSeriesProcessor;

import java.util.*;

public class SparseSequenceEncoder {

    private int quantization; //for quantization of continuous values
    private int nGrams; //how many grams are used for encoding sequences
    private final SparseIntervalEmbedding sequenceIntervalAtoms;
    private final List<SparseBinaryVector> gramVectorList = new ArrayList<>();
    private final Map<SparseBinaryVector, Number> associativeMemory = new HashMap<>();
    private final List<SparseBinaryVector> encodedSequences = new ArrayList<>();
    private int[] labels;
    private int numberClasses;

    private int[][] encodedSequencesClassic;

    private SparseBinaryVector latestResidual;


    private int numberDims;



    private static final double SLACK = 0.001;
    private SparseBinaryVector latestQuery;



    /**
     * Constructor for the SequenceEncoder class.
     * @param sequences the list of sequences to encode
     * @param quantization the number of quantization levels for continuous values
     * @param nGrams the number of grams to use for encoding
     */
    public SparseSequenceEncoder(double[][] sequences, int quantization, int nGrams) {

        this.quantization = quantization;
        this.nGrams = nGrams;
        for (int i = 0; i < nGrams; i++) {
            gramVectorList.add(SparseBinaryVector.rand());
        }

        double[] minMax = findMinMax(sequences);

        System.out.println("minMax: " + Arrays.toString(minMax));

        sequenceIntervalAtoms = new SparseIntervalEmbedding(minMax[0]-.0001, minMax[1]+.0001, quantization);

        encodeSequences(sequences);
        buildAssociativeMemory(sequences);


        //print out quantization and nGrams
        System.out.println("Quantization: " + quantization);
        System.out.println("nGrams: " + nGrams);
    }


    /**
     * Initializes sequences for learning
     * @param sequences
     * @param labels
     * @param quantization
     * @param nGrams
     */
    public SparseSequenceEncoder(double[][] sequences, int[] labels, int quantization, int nGrams) {

        this.quantization = quantization;
        this.nGrams = nGrams;
        for (int i = 0; i < nGrams; i++) {
            gramVectorList.add(SparseBinaryVector.rand());
        }

        double[] minMax = findMinMax(sequences);
        this.labels = labels;

        System.out.println("minMax: " + Arrays.toString(minMax));

        sequenceIntervalAtoms = new SparseIntervalEmbedding(minMax[0]-SLACK, minMax[1]+SLACK, quantization);

        encodeSequences(sequences);
        buildAssociativeMemory(sequences);


        //print out quantization and nGrams
        System.out.println("Quantization: " + quantization);
        System.out.println("nGrams: " + nGrams);
    }


    public SparseSequenceEncoder(List<TimeSeriesProcessor.TimeSeriesData> series, int quantization, int nGrams) {

        this.quantization = quantization;
        this.nGrams = nGrams;
        for (int i = 0; i < nGrams; i++) {
            gramVectorList.add(SparseBinaryVector.rand());
        }

        double[][] sequences = new double[series.size()][];
        labels = new int[series.size()];



        for (int i = 0; i < series.size(); i++) {
            labels[i] = (int) series.get(i).getLabel();
            sequences[i] = series.get(i).getValues();
        }

        //how many distinct classes are there?
        numberClasses = (int) Arrays.stream(labels).distinct().count();

        //normalize labels such that they are 0, 1, 2, 3, etc., with 0 being the most common class, etc
        Map<Integer, Integer> labelMap = new HashMap<>();
        int labelIndex = 0;
        for (int i = 0; i < labels.length; i++) {
            if (!labelMap.containsKey(labels[i])) {
                labelMap.put(labels[i], labelIndex);
                labelIndex++;
            }
        }

        for (int i = 0; i < labels.length; i++) {
            labels[i] = labelMap.get(labels[i]);
        }

        double[] minMax = findMinMax(sequences);

        sequenceIntervalAtoms = new SparseIntervalEmbedding(minMax[0]-SLACK, minMax[1]+SLACK, quantization);

        encodeSequences(sequences);
        //buildAssociativeMemory(sequences);
        encodedSequencesClassic = classicEncoding(sequences, minMax[0]-SLACK, minMax[1]+SLACK, quantization);

        numberDims = encodedSequencesClassic[0].length;

    }

    public int[][] classicEncoding(double[][] sequences, double min, double max, int quantization) {


        int[][] encodedSequences = new int[sequences.length][];
        //for every sequence, encode it using Quantization
        for (int i = 0; i < sequences.length; i++) {
            encodedSequences[i] = encodeSequenceClassic(sequences[i], min, max, quantization);
        }

        return encodedSequences;
    }

    private int[] encodeSequenceClassic(double[] s, double min, double max, int numberDims) {

        //for every value in the sequence, quantize it and add it to a List, then concatenate the List into an array
        List<int[]> encodingList = new ArrayList<>();
        for (double value : s) {
            encodingList.add(quantizeValue(min, max, numberDims, value));
        }

        return concatenateArrays(encodingList);

    }

    private int[] quantizeValue(double min, double max, int numberDims, double value) {

        int[] encoding = new int[numberDims];

        // Calculate the width of each bucket
        double bucketWidth = (max - min) / (numberDims - 1);

        // Determine the number of buckets that should be "filled"
        int numFilledBuckets = (int) Math.ceil((value - min) / bucketWidth);

        // Ensure that the number of filled buckets does not exceed numberDims
        numFilledBuckets = Math.min(numFilledBuckets, numberDims);

        // Fill the buckets up to numFilledBuckets
        for (int i = 0; i < numFilledBuckets; i++) {
            encoding[i] = 1;
        }

        return encoding;
    }


    /**
     * Encodes a sequence of data into a fixed length vector.
     * Must be same length as the Gram length used for encoding.
     * Used for the associative memory prediction
     * @param gramSequence
     * @return
     */
    public SparseBinaryVector encodeGramSequence(double[] gramSequence) {

        if(gramSequence.length < nGrams) {
            throw new IllegalArgumentException("Sequence length must be equal to the number of grams used for encoding: " + nGrams + " not equal to: " + gramSequence.length);
        }

        //if gramSequence is longer than nGrams, take the last nGrams elements
        if(gramSequence.length > nGrams) {
            gramSequence = Arrays.copyOfRange(gramSequence, gramSequence.length - nGrams, gramSequence.length);
        }

        return encodeSequence(gramSequence, gramVectorList, sequenceIntervalAtoms, nGrams);
    }

    public Number predictNextElement(SparseBinaryVector queryVector) {
        Number bestMatch = -1;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<SparseBinaryVector, Number> entry : associativeMemory.entrySet()) {
            int distance =  queryVector.hammingDistance(entry.getKey());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = entry.getValue();

                computeResidual(entry.getKey(), queryVector);
                //System.out.println("Distance: " + minDistance);
            }
        }
        return bestMatch;
    }

    public Number predict(double[] sequence) {
        SparseBinaryVector queryVector = encodeGramSequence(sequence);
        return predictNextElement(queryVector);
    }


    /**
     * Encodes a list of sequences into a list of fixed length vectors.
     * @param sequences the list of sequences to encode
     */
    public void encodeSequences(double[][] sequences) {

        int nSeqs = sequences.length;
        for (int i = 0; i < nSeqs; i++) {
            SparseBinaryVector sequenceVector = encodeSequence(sequences[i], gramVectorList, sequenceIntervalAtoms, nGrams);
            encodedSequences.add(sequenceVector);
        }
    }

    /**
     * Encode one sequence of data into a fixed length vector.
     * @param sequence
     * @return
     */
    public SparseBinaryVector encodeSequence(double[] sequence) {
        return encodeSequence(sequence, gramVectorList, sequenceIntervalAtoms, nGrams);
    }

    /**
     * Builds an associative memory for the sequences.
     * @param sequences the list of sequences to build the associative memory for
     */
    public void buildAssociativeMemory(double[][] sequences) {

        // Encode and store the sequences in associative memory
        int nGramSize = nGrams;

        for (double[] sequence : sequences) {
            for (int i = 0; i <= sequence.length - nGramSize; i++) {
                double[] nGram = Arrays.copyOfRange(sequence, i, i + nGramSize);
                SparseBinaryVector encodedGram = encodeSequence(nGram, gramVectorList, sequenceIntervalAtoms, nGramSize);
                double nextElement = (i + nGramSize < sequence.length) ? sequence[i + nGramSize] : -1;
                associativeMemory.put(encodedGram, nextElement);
            }
        }
    }


    /**
     * Encodes a sequence of data into a fixed length vector.
     * @param sequence the sequence of data to encode
     * @param gramVectors the list of gram vectors to use for encoding
     * @param sequenceIntervalAtoms the interval embedding for the sequence
     * @param n the number of grams to use for encoding
     * @return the encoded sequence
     */
    public static SparseBinaryVector encodeSequence(double[] sequence, List<SparseBinaryVector> gramVectors, SparseIntervalEmbedding sequenceIntervalAtoms, int n) {

        int length = sequence.length;
        int numGrams = length - n + 1;

        // Generate n-grams with temporal binding
        List<SparseBinaryVector> nGrams = new ArrayList<>();

        SparseBinaryVector nGramVector;

        for(int i = 0; i < numGrams; i++) {

            nGramVector = SparseBinaryVector.zeroVector();
            for (int j = 0; j < n; j++) {

                SparseBinaryVector permutedSj = sequenceIntervalAtoms.forward(sequence[i + j]).permute(j);
                SparseBinaryVector combinedVector = permutedSj.bind(gramVectors.get(j));
                nGramVector = nGramVector.bind(permutedSj);
            }
            nGrams.add(nGramVector);
        }

        // Aggregate n-grams to form the final sequence vector
        return SparseBinaryVector.bundle(nGrams);

    }


    public void computeResidual(SparseBinaryVector sequenceVector, SparseBinaryVector queryVector) {
        latestResidual = sequenceVector.bind(queryVector);
        latestQuery = queryVector;
    }


    public static double[] findMinMax(double[][] sequences) {

        //first find the min and max values of the sequences
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double[] sequence : sequences) {
            for (double value : sequence) {
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }
        return new double[]{min, max};

    }

    public Map<SparseBinaryVector, Number> getAssociativeMemory() {
        return associativeMemory;
    }

    public List<SparseBinaryVector> getEncodedSequences() {
        return encodedSequences;
    }

    public int getLabel(int index) {
        return labels[index];
    }

    public int getNumberClasses() {
        return numberClasses;
    }

    public static int[] concatenateArrays(List<int[] > arrays) {
        // Calculate the total length of the concatenated array
        int totalLength = 0;
        for (int[] array : arrays) {
            totalLength += array.length;
        }

        // Create the resulting array with the calculated length
        int[] result = new int[totalLength];

        // Copy each array into the result array
        int currentPosition = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, result, currentPosition, array.length);
            currentPosition += array.length;
        }

        return result;
    }

    public int getNumberDims() {
        return numberDims;
    }
    public int[][] getEncodedSequencesClassic() {
        return encodedSequencesClassic;
    }

    public SparseBinaryVector getLatestResidual() {
        return latestResidual;
    }

    public SparseBinaryVector getLatestQuery() {
        return latestQuery;
    }

}
