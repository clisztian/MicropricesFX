package records;

import encoders.Temporal;

public class TimeIndicator {
    public String getName() {
        return name;
    }

    public TimeIndicator(String name, String type, Temporal timestamp, double value) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Temporal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Temporal timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private String name;
    private String type;
    private Temporal timestamp;
    private double value;

}

