package ucr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TimeSeriesProcessor {

    public static class TimeSeriesData {
        private double label;
        private double[] values;

        public TimeSeriesData(double label, double[] values) {
            this.label = label;
            this.values = values;
        }

        public double getLabel() {
            return label;
        }

        public double[] getValues() {
            return values;
        }

        public void setValues(double[] values) {
            this.values = values;
        }
    }

    public static List<TimeSeriesData> readTimeSeriesFile(String filePath) throws IOException {
        List<TimeSeriesData> dataList = new ArrayList<>();
        double sum = 0;
        int count = 0;

        // First pass: read the file and calculate the sum and count of valid values
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t"); // Split by tab
                for (int i = 1; i < parts.length; i++) {
                    if (!parts[i].equalsIgnoreCase("NA")) {
                        sum += Double.parseDouble(parts[i]);
                        count++;
                    }
                }
            }
        }

        double mean = count == 0 ? 0 : sum / count;

        // Second pass: read the file again, replace NA with mean, and create TimeSeriesData objects
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t"); // Split by tab
                double label = Double.parseDouble(parts[0]);
                double[] values = new double[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i].equalsIgnoreCase("NA")) {
                        values[i - 1] = mean;
                    } else {
                        values[i - 1] = Double.parseDouble(parts[i]);
                    }
                }
                dataList.add(new TimeSeriesData(label, values));
            }
        }
        return dataList;
    }

    public static double[] computeStats(List<TimeSeriesData> dataList) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        int count = 0;

        for (TimeSeriesData data : dataList) {
            for (double value : data.getValues()) {
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
                sum += value;
                count++;
            }
        }

        double mean = count == 0 ? 0 : sum / count;
        return new double[]{min, mean, max};
    }

}
