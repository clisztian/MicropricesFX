package records;

import encoders.Temporal;

public class TimeTrois {
    public TimeTrois(Temporal time, double val_1, double val_2, double val_3) {
        this.time = time;
        this.val_1 = val_1;
        this.val_2 = val_2;
        this.val_3 = val_3;
    }

    private Temporal time;

    public Temporal getTime() {
        return time;
    }

    public void setTime(Temporal time) {
        this.time = time;
    }

    public double getVal_1() {
        return val_1;
    }

    public void setVal_1(double val_1) {
        this.val_1 = val_1;
    }

    public double getVal_2() {
        return val_2;
    }

    public void setVal_2(double val_2) {
        this.val_2 = val_2;
    }

    public double getVal_3() {
        return val_3;
    }

    public void setVal_3(double val_3) {
        this.val_3 = val_3;
    }

    private double val_1;
        private double val_2;
        private double val_3;
}