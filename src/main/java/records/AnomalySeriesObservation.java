package records;

import encoders.Temporal;

public class AnomalySeriesObservation {

    private Temporal timestamp;
    private float value;

    public Temporal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Temporal timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public AnomalySeriesObservation(Temporal timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
