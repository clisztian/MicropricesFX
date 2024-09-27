package hyperdimension.encoders;

import util.HVC;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VanillaBHV  {
    private byte[] data;
    public static final int DIMENSION = HVC.DIMENSION;
    public static final int BYTE_SIZE = HVC.BYTE_SIZE;
    private static final VanillaBHV ZERO = new VanillaBHV(new byte[BYTE_SIZE]);
    private static final VanillaBHV ONE = new VanillaBHV(new byte[BYTE_SIZE]);

    static {
        Arrays.fill(ONE.data, (byte) 0xff);
    }

    public VanillaBHV(byte[] data) {
        this.data = data;
    }

    public static VanillaBHV randVector() {
        Random random = new Random();
        byte[] data = new byte[BYTE_SIZE];
        random.nextBytes(data);
        return new VanillaBHV(data);
    }

    public static VanillaBHV zeroVector() {
        return ZERO;
    }

    public VanillaBHV rollBytes(int n) {
        byte[] rolled = new byte[BYTE_SIZE];
        System.arraycopy(this.data, n, rolled, 0, BYTE_SIZE - n);
        System.arraycopy(this.data, 0, rolled, BYTE_SIZE - n, n);
        return new VanillaBHV(rolled);
    }

    public VanillaBHV rollBits(int n) {
        n = (DIMENSION + n) % DIMENSION;
        return fromInt((this.toInt() << (DIMENSION - n)) & ONE.toInt() | this.toInt() >> n);
    }

    public VanillaBHV permuteBytes(VanillaPermutation permutation) {
        byte[] permuted = new byte[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            permuted[i] = this.data[permutation.data[i]];
        }
        return new VanillaBHV(permuted);
    }

    public VanillaBHV rehash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new VanillaBHV(digest.digest(this.data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public int toInt() {
        return new BigInteger(1, this.data).intValue();
    }

    public static VanillaBHV fromInt(int i) {
        return new VanillaBHV(BigInteger.valueOf(i).toByteArray());
    }

    public boolean equals(VanillaBHV other) {
        return Arrays.equals(this.data, other.data);
    }

    public VanillaBHV xor(VanillaBHV other) {
        byte[] result = new byte[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            result[i] = (byte) (this.data[i] ^ other.data[i]);
        }
        return new VanillaBHV(result);
    }

    public VanillaBHV and(VanillaBHV other) {
        byte[] result = new byte[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            result[i] = (byte) (this.data[i] & other.data[i]);
        }
        return new VanillaBHV(result);
    }

    public VanillaBHV or(VanillaBHV other) {
        byte[] result = new byte[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            result[i] = (byte) (this.data[i] | other.data[i]);
        }
        return new VanillaBHV(result);
    }

    public int active() {
        return Integer.bitCount(this.toInt());
    }

    public static VanillaBHV fromBytes(byte[] bs) {
        return new VanillaBHV(bs);
    }

    public byte[] toBytes() {
        return this.data;
    }

    public String bitstring() {
        return new BigInteger(1, this.data).toString(2);
    }

    /*
    Output data as a boolean vector
     */
    public boolean[] toBooleanVector() {
        boolean[] vector = new boolean[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = (this.data[i / HVC.SIZE] & (1 << (i % HVC.SIZE))) != 0;
        }
        return vector;
    }

    /*
     Output data as an int array
     */
    public int[] toBooleanIntArray() {
        int[] vector = new int[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = (this.data[i / HVC.SIZE] & (1 << (i % HVC.SIZE))) != 0 ? 1 : 0;
        }
        return vector;
    }

    /*
    Output data as an int array using unsigned bytes
     */



    /**
     *
     * @param positions
     * @return
     */
    public VanillaBHV permute(int positions) {
        int bitShift = positions % DIMENSION;
        if (bitShift < 0) {
            bitShift += DIMENSION;
        }
        return shiftBits(bitShift);
    }

    private VanillaBHV shiftBits(int bitShift) {
        int byteShift = bitShift / HVC.SIZE;
        int bitOffset = bitShift % HVC.SIZE;

        byte[] result = new byte[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            int nextIndex = (i + byteShift) % BYTE_SIZE;
            int prevIndex = (i - 1 + BYTE_SIZE) % BYTE_SIZE;

            int currentByte = (data[i] & 0xFF) << bitOffset;
            int previousByte = (data[prevIndex] & 0xFF) >>> (HVC.SIZE - bitOffset);
            result[nextIndex] = (byte) (currentByte | previousByte);
        }

        return new VanillaBHV(result);
    }



    public static VanillaBHV logic_majority(List<VanillaBHV> vectors) {
        int[] counts = new int[DIMENSION];
        for (VanillaBHV vector : vectors) {
            byte[] data = vector.toBytes();
            for (int i = 0; i < BYTE_SIZE; i++) {
                for (int bit = 0; bit < HVC.SIZE; bit++) {
                    if ((data[i] & (1 << bit)) != 0) {
                        counts[i * HVC.SIZE + bit]++;
                    }
                }
            }
        }

        byte[] majorityData = new byte[BYTE_SIZE];
        int threshold = vectors.size() / 2;
        for (int i = 0; i < BYTE_SIZE; i++) {
            for (int bit = 0; bit < HVC.SIZE; bit++) {
                if (counts[i * HVC.SIZE + bit] > threshold) {
                    majorityData[i] |= 1 << bit;
                }
            }
        }
        return new VanillaBHV(majorityData);
    }

    //related if the hamming distance is less than or equal 40 percent of the DIMEENSION
    public boolean related(VanillaBHV other) {
        return this.hammingDistance(other) <= DIMENSION * HVC.RELATED_THRESHOLD;
    }

    public int hammingDistance(VanillaBHV other) {
        int distance = 0;
        for (int i = 0; i < BYTE_SIZE; i++) {
            distance += Integer.bitCount((this.data[i] & 0xFF) ^ (other.data[i] & 0xFF));
        }
        return distance;
    }

    public static int hammingDistanceBoolean(boolean[] vector1, boolean[] vector2) {
        int distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            if (vector1[i] != vector2[i]) {
                distance++;
            }
        }
        return distance;
    }

    //compare two boolean vectors
    public static double hammingDistance(boolean[] vector1, boolean[] vector2) {
        int distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            if (vector1[i] != vector2[i]) {
                distance++;
            }
        }
        return (double) distance / vector1.length;
    }

    public int[] toIntArray() {
        int[] intArray = new int[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            intArray[i] = data[i] & 0xFF;
        }
        return intArray;
    }

    public static void main(String[] args) {
        // Test VanillaPermutation
        VanillaPermutation perm1 = VanillaPermutation.random();
        VanillaPermutation perm2 = VanillaPermutation.random();

        System.out.println("Permutation 1: " + Arrays.toString(perm1.data));
        System.out.println("Permutation 2: " + Arrays.toString(perm2.data));

        VanillaPermutation perm3 = perm1.multiply(perm2);
        System.out.println("Permutation 3 (perm1 * perm2): " + Arrays.toString(perm3.data));

        VanillaPermutation perm1Inv = perm1.invert();
        System.out.println("Permutation 1 Inverted: " + Arrays.toString(perm1Inv.data));

        // Test VanillaBHV
        VanillaBHV hv1 = VanillaBHV.randVector();
        VanillaBHV hv2 = VanillaBHV.randVector();

        System.out.println("HV1: " + Arrays.toString(hv1.toBytes()));
        System.out.println("HV2: " + Arrays.toString(hv2.toBytes()));

        VanillaBHV hv3 = hv1.xor(hv2);
        System.out.println("HV3 (hv1 XOR hv2): " + Arrays.toString(hv3.toBytes()));

        VanillaBHV hv4 = hv1.permuteBytes(perm1);
        System.out.println("HV4 (hv1 permuted by perm1): " + Arrays.toString(hv4.toBytes()));

        VanillaBHV hv5 = hv4.permuteBytes(perm1Inv);
        System.out.println("HV5 (hv4 permuted back by perm1Inv): " + Arrays.toString(hv5.toBytes()));

        // Check if the permutation and its inverse result in the original HV
        System.out.println("HV1 equals HV5: " + hv1.equals(hv5));
        System.out.println("HV1 active bits: " + hv1.active());
        System.out.println("Hamming on byts: " + hv1.hammingDistance(hv5));

        System.out.println("Booleanized hamming " + hammingDistance(hv1.toBooleanVector(), hv5.toBooleanVector()));

        VanillaBHV vector1 = VanillaBHV.randVector ();
        VanillaBHV vector2 = VanillaBHV.randVector();

        int distance = vector1.hammingDistance(vector2);
        int maxDistance = DIMENSION;

        System.out.println("Hamming Distance: " + distance);
        System.out.println("Max Possible Distance: " + maxDistance);

        if (distance <= maxDistance) {
            System.out.println("Hamming distance is within the expected range.");
        } else {
            System.out.println("Hamming distance is out of the expected range. Check the implementation.");
        }


        int[] array = hv5.toIntArray();
        System.out.println("array: " + array.length + " " + HVC.SIZE + " " + DIMENSION);
        System.out.println(Arrays.toString(array));


    }



}
