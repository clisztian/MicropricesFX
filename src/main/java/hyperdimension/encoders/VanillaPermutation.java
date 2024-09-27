package hyperdimension.encoders;

import util.HVC;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VanillaPermutation {
    private static final int BYTE_SIZE = HVC.BYTE_SIZE;
    public int[] data;
    private static final VanillaPermutation IDENTITY = new VanillaPermutation(IntStream.range(0, BYTE_SIZE).toArray());

    private static final Random RANDOM = new Random();

    private VanillaPermutation(int[] indices) {
        this.data = indices;
    }

    public static VanillaPermutation random() {
        List<Integer> p = IntStream.range(0, BYTE_SIZE).boxed().collect(Collectors.toList());
        Collections.shuffle(p);
        return new VanillaPermutation(p.stream().mapToInt(i -> i).toArray());
    }

    public VanillaPermutation multiply(VanillaPermutation other) {
        int[] result = new int[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            result[i] = this.data[other.data[i]];
        }
        return new VanillaPermutation(result);
    }

    public VanillaPermutation invert() {
        int[] p = new int[BYTE_SIZE];
        for (int i = 0; i < BYTE_SIZE; i++) {
            p[this.data[i]] = i;
        }
        return new VanillaPermutation(p);
    }

    public VanillaBHV apply(VanillaBHV hv) {
        return hv.permuteBytes(this);
    }

    public static VanillaPermutation getIdentity() {
        return IDENTITY;
    }
}
