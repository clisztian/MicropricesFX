package output;

public class OutputStats {

	private int label_class;
	private int true_output; //how many times true output
	private int pred_output_correct; //how many times predicted correct
	private int false_positive; //how many times false 
	
	
	public void true_output_inc() {
		true_output++;
	}
	
	public void pred_output_correct_inc() {
		pred_output_correct++;
	}
	
	public void false_positive_inc() {
		false_positive++;
	}
	
	public int getLabel_class() {
		return label_class;
	}
	public void setLabel_class(int label_class) {
		this.label_class = label_class;
	}
	public int getTrue_output() {
		return true_output;
	}
	public void setTrue_output(int true_output) {
		this.true_output = true_output;
	}
	public int getPred_output_correct() {
		return pred_output_correct;
	}
	public void setPred_output_correct(int pred_output_correct) {
		this.pred_output_correct = pred_output_correct;
	}
	public int getFalse_positive() {
		return false_positive;
	}
	public void setFalse_positive(int false_positive) {
		this.false_positive = false_positive;
	}

	public void clear() {

		true_output = 0;
		pred_output_correct = 0;
		false_positive = 0;	
	}
	
}
