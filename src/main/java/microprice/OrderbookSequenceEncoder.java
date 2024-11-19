package microprice;

import hyperdimension.encoders.VanillaBHV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to encode sequences of orderbook states using the N Grams method.
 */
public class OrderbookSequenceEncoder {

    private int nGrams; //how many grams are used for encoding sequences
    private final List<VanillaBHV> gramVectorList = new ArrayList<>();
    private final Map<VanillaBHV, Number> associativeMemory = new HashMap<>();
    private final List<VanillaBHV> encodedSequences = new ArrayList<>();


    public OrderbookSequenceEncoder(int nGrams) {
        this.nGrams = nGrams;
        for (int i = 0; i < nGrams; i++) {
            gramVectorList.add(VanillaBHV.randVector());
        }
    }


    /**
     * Encodes a sequence of orderbook states using the N Grams method.
     * @param sequence
     * @return
     */
    public VanillaBHV encodeSequence(List<VanillaBHV> sequence) {

        if(sequence.size() < nGrams) {
            throw new IllegalArgumentException("The sequence is too short to be encoded with the given nGrams value.");
        }

        int length = sequence.size();
        int numGrams = length - nGrams + 1;

        List<VanillaBHV> gramVector = new ArrayList<>();
        VanillaBHV nGramVector;
        for(int i = 0; i < numGrams; i++) {

            nGramVector = VanillaBHV.zeroVector();
             for (int j = 0; j < nGrams; j++) {

                  VanillaBHV permutedSj = sequence.get(i + j).permute(j);
                  //VanillaBHV combinedVector = permutedSj.xor(gramVectorList.get(j));
                  nGramVector = nGramVector.xor(permutedSj);
              }
              gramVector.add(nGramVector);
        }
        return VanillaBHV.logic_majority(gramVector);
    }









    public int getnGrams() {
        return nGrams;
    }

    public void setnGrams(int nGrams) {
        this.nGrams = nGrams;
    }

    public List<VanillaBHV> getGramVectorList() {
        return gramVectorList;
    }

    public Map<VanillaBHV, Number> getAssociativeMemory() {
        return associativeMemory;
    }

    public List<VanillaBHV> getEncodedSequences() {
        return encodedSequences;
    }
}
