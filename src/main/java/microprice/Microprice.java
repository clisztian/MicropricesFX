package microprice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Microprice {
    private double askRank1;
    private double askRank2;
    private double askRank3;
    private double askRank4;
    private double askRank5;
    private double bidRank1;
    private double bidRank2;
    private double bidRank3;
    private double bidRank4;
    private double bidRank5;
    private double spreadSize;
    private double priceChange;
    private double lastXTrades;

    private double imbalance;



    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAskRank1() {
        return askRank1;
    }

    public void setAskRank1(double askRank1) {
        this.askRank1 = askRank1;
    }

    public double getAskRank2() {
        return askRank2;
    }

    public void setAskRank2(double askRank2) {
        this.askRank2 = askRank2;
    }

    public double getAskRank3() {
        return askRank3;
    }

    public void setAskRank3(double askRank3) {
        this.askRank3 = askRank3;
    }

    public double getAskRank4() {
        return askRank4;
    }

    public void setAskRank4(double askRank4) {
        this.askRank4 = askRank4;
    }

    public double getAskRank5() {
        return askRank5;
    }

    public void setAskRank5(double askRank5) {
        this.askRank5 = askRank5;
    }

    public double getBidRank1() {
        return bidRank1;
    }

    public void setBidRank1(double bidRank1) {
        this.bidRank1 = bidRank1;
    }

    public double getBidRank2() {
        return bidRank2;
    }

    public void setBidRank2(double bidRank2) {
        this.bidRank2 = bidRank2;
    }

    public double getBidRank3() {
        return bidRank3;
    }

    public void setBidRank3(double bidRank3) {
        this.bidRank3 = bidRank3;
    }

    public double getBidRank4() {
        return bidRank4;
    }

    public void setBidRank4(double bidRank4) {
        this.bidRank4 = bidRank4;
    }

    public double getBidRank5() {
        return bidRank5;
    }

    public void setBidRank5(double bidRank5) {
        this.bidRank5 = bidRank5;
    }

    public double getSpreadSize() {
        return spreadSize;
    }

    public void setSpreadSize(double spreadSize) {
        this.spreadSize = spreadSize;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public double getLastXTrades() {
        return lastXTrades;
    }

    public void setLastXTrades(double lastXTrades) {
        this.lastXTrades = lastXTrades;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void setMicroPrice(double microPrice) {
        this.microPrice = microPrice;
    }

    private double lastPrice;
    private double microPrice;

    private double adjustedMicroprice;

    public double getImbalance() {
        return imbalance;
    }

    public void setImbalance(double imbalance) {
        this.imbalance = imbalance;
    }

    public void setAdjustedMicroprice(double adjustedMicroprice) {
        this.adjustedMicroprice = adjustedMicroprice;
    }

    // Constructor
    public Microprice(double askRank1, double askRank2, double askRank3, double askRank4, double askRank5,
                      double bidRank1, double bidRank2, double bidRank3, double bidRank4, double bidRank5,
                      double spreadSize, double priceChange, double lastXTrades, double lastPrice, double microPrice) {
        this.askRank1 = askRank1;
        this.askRank2 = askRank2;
        this.askRank3 = askRank3;
        this.askRank4 = askRank4;
        this.askRank5 = askRank5;
        this.bidRank1 = bidRank1;
        this.bidRank2 = bidRank2;
        this.bidRank3 = bidRank3;
        this.bidRank4 = bidRank4;
        this.bidRank5 = bidRank5;
        this.spreadSize = spreadSize;
        this.priceChange = priceChange;
        this.lastXTrades = lastXTrades;
        this.lastPrice = lastPrice;
        this.microPrice = microPrice;
    }

    // Getters and toString() for displaying the data
    public double getMicroPrice() {
        return microPrice;
    }

    public double getRawMicroPrice() {
        return adjustedMicroprice;
    }

    @Override
    public String toString() {
        return "Microprice{" +
                "askRank1=" + askRank1 +
                ", askRank2=" + askRank2 +
                ", askRank3=" + askRank3 +
                ", askRank4=" + askRank4 +
                ", askRank5=" + askRank5 +
                ", bidRank1=" + bidRank1 +
                ", bidRank2=" + bidRank2 +
                ", bidRank3=" + bidRank3 +
                ", bidRank4=" + bidRank4 +
                ", bidRank5=" + bidRank5 +
                ", spreadSize=" + spreadSize +
                ", priceChange=" + priceChange +
                ", lastXTrades=" + lastXTrades +
                ", lastPrice=" + lastPrice +
                ", microPrice=" + microPrice +
                '}';
    }

    public double getAdjustedMicroprice() {
        return microPrice - lastPrice;
    }

    public void setRawMicroprice(double v) {
        this.adjustedMicroprice = v;
    }
}

class MicropriceReader {
    public static List<Microprice> readMicroprices(String filename) {

        List<Microprice> microprices = new ArrayList<>();

        ClassLoader classLoader = MicropriceReader.class.getClassLoader();
        File file = new File(classLoader.getResource("microprice/" + filename).getFile());



        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                Microprice microprice = new Microprice(
                        Double.parseDouble(values[1]),
                        Double.parseDouble(values[2]),
                        Double.parseDouble(values[3]),
                        Double.parseDouble(values[4]),
                        Double.parseDouble(values[5]),
                        Double.parseDouble(values[6]),
                        Double.parseDouble(values[7]),
                        Double.parseDouble(values[8]),
                        Double.parseDouble(values[9]),
                        Double.parseDouble(values[10]),
                        Double.parseDouble(values[11]),
                        Double.parseDouble(values[12]),
                        Double.parseDouble(values[13]),
                        Double.parseDouble(values[14]),
                        Double.parseDouble(values[16])
                );
                microprice.setTimestamp(values[0]);
                microprice.setImbalance(Double.parseDouble(values[17]));
                microprice.setRawMicroprice(Double.parseDouble(values[15]));
                microprices.add(microprice);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return microprices;
    }

    public static void main(String[] args) {
        List<Microprice> microprices = readMicroprices("microprice_tsla.csv");
        for (Microprice mp : microprices) {
            System.out.println(mp);
        }
    }



}
