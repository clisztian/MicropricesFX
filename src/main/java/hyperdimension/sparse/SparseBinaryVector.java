package hyperdimension.sparse;

import util.HVC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SparseBinaryVector {
    public long[] segments;
    private int S; // Number of segments
    private int L; // Length of each segment (maximum 64 bits since long is 64 bits)

    private int DIMENSION;



    // Constructor to initialize the sparse binary vector
    public SparseBinaryVector(int S, int L) {
        if (L > 64) {
            throw new IllegalArgumentException("Segment length L cannot be greater than 64 for long representation.");
        }
        this.S = S;
        this.L = L;
        this.segments = new long[S];  // Each segment is represented by a long integer

        this.DIMENSION = S * L;
        // Initialize the vector with sparse binary values
        generateSparseVector();
    }

    public static SparseBinaryVector rand() {
        return new SparseBinaryVector(HVC.SEGMENTS, HVC.LONG_SIZE);
    }

    public static SparseBinaryVector zeroVector() {
        SparseBinaryVector zeroVector = new SparseBinaryVector(HVC.SEGMENTS, HVC.LONG_SIZE);
        Arrays.fill(zeroVector.segments, 0L);
        return zeroVector;
    }

    // Method to generate the sparse binary vector
    private void generateSparseVector() {
        Random random = new Random();

        // For each segment, set one random bit to 1
        for (int i = 0; i < S; i++) {
            int randomBit = random.nextInt(L); // Random bit position within the segment
            segments[i] = 1L << randomBit;     // Set the bit at 'randomBit' position
        }
    }

    // Permutation Operation: Circular shift right on segments (not bits within each segment)
    public void permute() {
        if (S > 1) {
            long lastSegment = segments[S - 1]; // Store the last segment temporarily
            // Shift all segments right
            for (int i = S - 1; i > 0; i--) {
                segments[i] = segments[i - 1];
            }
            segments[0] = lastSegment; // Move the last segment to the first position
        }
    }

    // Permutation Operation: Circular shift right on segments j times (not bits within each segment)
    // Permute the vector by shifting segments j places using circular shift
    public SparseBinaryVector permute(int j) {
        SparseBinaryVector result = new SparseBinaryVector(S, L);
        int shift = (j % S + S) % S;  // Handle both positive and negative shifts

        // Perform circular shift
        for (int i = 0; i < S; i++) {
            result.segments[(i + shift) % S] = this.segments[i];
        }

        return result;
    }

    // Inverse Permutation Operation: Circular shift left on segments
    public void inversePermute() {
        if (S > 1) {
            long firstSegment = segments[0]; // Store the first segment temporarily
            // Shift all segments left
            for (int i = 0; i < S - 1; i++) {
                segments[i] = segments[i + 1];
            }
            segments[S - 1] = firstSegment; // Move the first segment to the last position
        }
    }

    // Binding Operation: Bind two vectors by circular shifting based on bit positions
    public SparseBinaryVector bind(SparseBinaryVector other) {
        if (this.S != other.S || this.L != other.L) {
            throw new IllegalArgumentException("Vectors must have the same size and segment length for binding.");
        }

        SparseBinaryVector result = new SparseBinaryVector(S, L);
        for (int i = 0; i < S; i++) {
            // Use the index of the set bit in this segment to circular shift the corresponding segment in 'other'
            int shift = Long.numberOfTrailingZeros(this.segments[i]);
            result.segments[i] = Long.rotateRight(other.segments[i], shift);
        }
        return result;
    }

    // Inverse Binding Operation: Unbind two vectors to restore the original vector
    public SparseBinaryVector inverseBind(SparseBinaryVector boundResult) {
        if (this.S != boundResult.S || this.L != boundResult.L) {
            throw new IllegalArgumentException("Vectors must have the same size and segment length for inverse binding.");
        }

        SparseBinaryVector originalVector = new SparseBinaryVector(S, L);
        for (int i = 0; i < S; i++) {
            // Use the index of the set bit in this segment to circular shift the corresponding segment in 'boundResult' in the opposite direction
            int shift = Long.numberOfTrailingZeros(this.segments[i]); // The set bit in vector A
            originalVector.segments[i] = Long.rotateLeft(boundResult.segments[i], shift); // Undo the circular shift
        }
        return originalVector;
    }

    // Sumset Operation: Logical OR between two vectors
    public SparseBinaryVector sumset(SparseBinaryVector other) {
        if (this.S != other.S || this.L != other.L) {
            throw new IllegalArgumentException("Vectors must have the same size and segment length for sumset.");
        }

        SparseBinaryVector result = new SparseBinaryVector(S, L);
        for (int i = 0; i < S; i++) {
            result.segments[i] = this.segments[i] | other.segments[i];
        }
        result.thinning(); // Perform thinning operation to retain sparsity
        return result;
    }

    // Thinning Operation: Select only one bit in each segment to retain sparsity
    public void thinning() {
        Random random = new Random();
        for (int i = 0; i < S; i++) {
            // Count the number of set bits in this segment
            long segment = segments[i];
            if (Long.bitCount(segment) > 1) {
                // There are multiple bits set, so we need to thin it
                int selectedBit = random.nextInt(Long.bitCount(segment));  // Select one of the set bits
                long newSegment = 0L;
                for (int bitPos = 0; bitPos < L; bitPos++) {
                    if ((segment & (1L << bitPos)) != 0) {
                        if (selectedBit == 0) {
                            newSegment = 1L << bitPos;
                            break;
                        }
                        selectedBit--;
                    }
                }
                segments[i] = newSegment;  // Update the segment with only one bit set
            }
        }
    }

    // New bundling strategy: Majority vote per segment with random tie-breaking
    public static SparseBinaryVector bundle(List<SparseBinaryVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            throw new IllegalArgumentException("List of vectors cannot be null or empty.");
        }

        // Ensure all vectors have the same size and segment length
        int S = vectors.get(0).S;
        int L = vectors.get(0).L;
        SparseBinaryVector result = new SparseBinaryVector(S, L);
        Random random = new Random();

        // Iterate through each segment
        for (int segmentIndex = 0; segmentIndex < S; segmentIndex++) {
            // Array to count the number of ON bits at each index in the segment
            int[] bitCounts = new int[L];

            // Count the number of bits ON at each index across all vectors
            for (SparseBinaryVector vector : vectors) {
                long segment = vector.segments[segmentIndex];
                for (int bitPos = 0; bitPos < L; bitPos++) {
                    if ((segment & (1L << bitPos)) != 0) {
                        bitCounts[bitPos]++;
                    }
                }
            }

            // Find the index with the majority vote (and handle ties)
            List<Integer> candidates = new ArrayList<>();
            int maxCount = 0;
            for (int bitPos = 0; bitPos < L; bitPos++) {
                if (bitCounts[bitPos] > maxCount) {
                    maxCount = bitCounts[bitPos];
                    candidates.clear();
                    candidates.add(bitPos);
                } else if (bitCounts[bitPos] == maxCount) {
                    candidates.add(bitPos); // Tied candidates
                }
            }

            // Randomly break ties if there are multiple candidates
            int chosenBitPos = candidates.get(random.nextInt(candidates.size()));

            // Set the chosen bit in the result segment
            result.segments[segmentIndex] = 1L << chosenBitPos;
        }

        return result;
    }

    // Method to print the vector in binary form
    public void printVector() {
        for (int i = 0; i < S; i++) {
            System.out.println(String.format("Segment %d: %64s", i, Long.toBinaryString(segments[i])).replace(' ', '0'));
        }
    }

    // Method to get the segments array
    public long[] getSegments() {
        return segments;
    }

    // Method to return the entire vector as a flattened binary integer array
    public int[] toBinaryArray() {
        int[] binaryArray = new int[S * L]; // Total length N = S * L
        int index = 0;

        for (int i = 0; i < S; i++) {
            for (int bitPos = 0; bitPos < L; bitPos++) {
                // Check if the bit at position 'bitPos' is set in the segment
                binaryArray[index++] = (segments[i] & (1L << bitPos)) != 0 ? 1 : 0;
            }
        }

        return binaryArray;
    }

    // Method to compute Hamming distance between two vectors
    public int hammingDistance(SparseBinaryVector other) {
        if (this.S != other.S || this.L != other.L) {
            throw new IllegalArgumentException("Vectors must have the same size and segment length to compute Hamming distance.");
        }

        int distance = 0;
        for (int i = 0; i < S; i++) {
            // XOR the segments and count the differing bits
            long xorSegment = this.segments[i] ^ other.segments[i];
            distance += Long.bitCount(xorSegment);
        }

        return distance;
    }

    // Main method for testing
    public static void main(String[] args) {
        // Example: Create a vector with 4 segments, each of length 64 bits
        SparseBinaryVector sparseVector1 = new SparseBinaryVector(4, 64);
        SparseBinaryVector sparseVector2 = new SparseBinaryVector(4, 64);

        System.out.println("Original Vector 1:");
        sparseVector1.printVector();

        System.out.println("\nOriginal Vector 2:");
        sparseVector2.printVector();

        // Test Permutation
        sparseVector1.permute();
        System.out.println("\nVector 1 after Permutation:");
        sparseVector1.printVector();

        sparseVector1.inversePermute();
        System.out.println("\nVector 1 after Inverse Permutation:");
        sparseVector1.printVector();

        // Test Binding
        SparseBinaryVector boundVector = sparseVector1.bind(sparseVector2);
        System.out.println("\nBound Vector (1 ⊗ 2):");
        boundVector.printVector();

        // Test Sumset and Thinning
        SparseBinaryVector sumsetVector = sparseVector1.sumset(sparseVector2);
        System.out.println("\nSumset Vector (1 + 2):");
        sumsetVector.printVector();

        sumsetVector.thinning();
        System.out.println("\nSumset Vector after Thinning:");
        sumsetVector.printVector();



        // Create some test vectors
        SparseBinaryVector v1 = new SparseBinaryVector(1, 4); // Example 1: 0010
        SparseBinaryVector v2 = new SparseBinaryVector(1, 4); // Example 2: 0010
        SparseBinaryVector v3 = new SparseBinaryVector(1, 4); // Example 3: 0010
        SparseBinaryVector v4 = new SparseBinaryVector(1, 4); // Example 4: 0100
        SparseBinaryVector v5 = new SparseBinaryVector(1, 4); // Example 5: 1000
        SparseBinaryVector v6 = new SparseBinaryVector(1, 4); // Example 5: 1000
        SparseBinaryVector v7 = new SparseBinaryVector(1, 4); // Example 5: 1000

        // Simulate specific bit patterns
        v1.segments[0] = 0b0010;
        v2.segments[0] = 0b0010;
        v3.segments[0] = 0b0010;
        v4.segments[0] = 0b0100;
        v5.segments[0] = 0b1000;
        v6.segments[0] = 0b0100;
        v7.segments[0] = 0b0010;

        // Bundle the vectors
        List<SparseBinaryVector> vectors = Arrays.asList(v1, v2, v3, v4, v5, v6, v7);
        SparseBinaryVector bundled = SparseBinaryVector.bundle(vectors);

        System.out.println("Bundled Vector:");
        bundled.printVector();


    }

    public int getDIMENSION() {
        return DIMENSION;
    }
}
