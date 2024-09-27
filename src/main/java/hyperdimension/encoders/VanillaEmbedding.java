package hyperdimension.encoders;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VanillaEmbedding {
    private Map<String, VanillaBHV> hvs;

    public VanillaEmbedding() {
        this.hvs = new HashMap<>();
    }

    public VanillaBHV forward(String x) {
        return hvs.computeIfAbsent(x, k -> VanillaBHV.randVector());
    }

    public String back(VanillaBHV hv) {
        return hvs.entrySet().stream()
                .min(Comparator.comparingInt(e -> hv.hammingDistance(e.getValue())))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static void main(String[] args) {
        VanillaEmbedding nameEmbed = new VanillaEmbedding();
        VanillaBHV nameHV = nameEmbed.forward("John");
        System.out.println(nameEmbed.back(nameHV));
    }
}

