package hyperdimension.examples;

import hyperdimension.encoders.VanillaBHV;
import hyperdimension.encoders.VanillaPermutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hyperdimension.encoders.VanillaBHV.hammingDistanceBoolean;

public class GrandmaExample {
    // Define relation utilities
    public static final VanillaPermutation relSubject = VanillaPermutation.random();
    public static final VanillaPermutation relObject = VanillaPermutation.random();

    // Define relations
    public static final VanillaBHV motherOf = VanillaBHV.randVector();
    public static final VanillaBHV fatherOf = VanillaBHV.randVector();
    public static final VanillaBHV grandmotherOf = VanillaBHV.randVector();

    // Apply relationship
    private static VanillaBHV applyRel(VanillaBHV rel, VanillaBHV x, VanillaBHV y) {
        VanillaBHV sx = relSubject.apply(rel).xor(x);
        VanillaBHV sy = relObject.apply(rel).xor(y);
        List<VanillaBHV> list = new ArrayList<>();
        list.add(sx);
        list.add(sy);
        return VanillaBHV.logic_majority(list);
    }

    // Generate a sample for the rule
    private static VanillaBHV generateSample() {
        VanillaBHV personX = VanillaBHV.randVector();
        VanillaBHV personY = VanillaBHV.randVector();
        VanillaBHV personZ = VanillaBHV.randVector();

        VanillaBHV mxy = applyRel(motherOf, personX, personY);
        VanillaBHV fyz = applyRel(fatherOf, personY, personZ);
        List<VanillaBHV> list = new ArrayList<>();
        list.add(mxy);
        list.add(fyz);
        VanillaBHV gxz = applyRel(grandmotherOf, personX, personZ);

        return gxz.xor(VanillaBHV.logic_majority(list));
    }

    public static void main(String[] args) {
        // Generate the grandmother rule
        List<VanillaBHV> samples = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            samples.add(generateSample());
        }
        VanillaBHV grandmotherRule = VanillaBHV.logic_majority(samples);

        // Applying the grandmother rule
        VanillaBHV anna = VanillaBHV.randVector();
        VanillaBHV bill = VanillaBHV.randVector();
        VanillaBHV cid = VanillaBHV.randVector();

        VanillaBHV annaMotherOfBill = applyRel(motherOf, anna, bill);
        VanillaBHV billFatherOfCid = applyRel(fatherOf, bill, cid);

        List<VanillaBHV> relations = new ArrayList<>();
        relations.add(annaMotherOfBill);
        relations.add(billFatherOfCid);

        VanillaBHV calculatedAnnaGrandmotherOfCid = grandmotherRule.xor(VanillaBHV.logic_majority(relations));
        VanillaBHV actualAnnaGrandmotherOfCid = applyRel(grandmotherOf, anna, cid);

        System.out.println("calculatedAnnaGrandmotherOfCid: " + Arrays.toString(calculatedAnnaGrandmotherOfCid.toBytes()));
        System.out.println("actualAnnaGrandmotherOfCid: " + Arrays.toString(actualAnnaGrandmotherOfCid.toBytes()));

        int distance = calculatedAnnaGrandmotherOfCid.hammingDistance(actualAnnaGrandmotherOfCid);
        System.out.println("hamming distance: " + distance);
        System.out.println("hamming distance: " + hammingDistanceBoolean(calculatedAnnaGrandmotherOfCid.toBooleanVector(), actualAnnaGrandmotherOfCid.toBooleanVector()));

        if (calculatedAnnaGrandmotherOfCid.related(actualAnnaGrandmotherOfCid)) {
            System.out.println("The calculated and actual grandmother relationship match.");
        } else {
            System.out.println("The calculated and actual grandmother relationship do not match.");
        }
    }
}

