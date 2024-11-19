package util;

import java.util.ArrayList;

public class ReferenceMetrics {

	private float typeI_error;
	private float typeII_error;
	private float sensitivity;
	private float specificity;
	private float accuracy;
	private float error_rate;
	private float f1_score;
	private float f2_score;
	private float MCC;



	private float f_score;
	
	private ArrayList<Float> accuracyList;
	private ArrayList<Float> typeIList;
	private ArrayList<Float> typeIIList;
	private ArrayList<Float> f1List;
	private float meanAcc;
	private float meanTypeI;
	private float meanTypeII;
	private float meanF1;
	
	private float productivity_factor;
	
	public ReferenceMetrics() {
		
		accuracyList = new ArrayList<Float>();
		typeIList = new ArrayList<Float>();
		typeIIList = new ArrayList<Float>();
		f1List = new ArrayList<Float>();
	}
	
	
	public ReferenceMetrics(float true_positive_X, float true_negative_Y, float false_positive_Q, float false_negative_Z) {
		
		float X = true_positive_X;
		float Y = true_negative_Y;
		float Q = false_positive_Q;
		float Z = false_negative_Z;

		typeI_error = Q;
		typeII_error = Z;
		sensitivity = X/(X+Z);
		specificity = Y/(Q+Y);
		accuracy = (X + Y)/(X + Y + Q + Z);
		error_rate = 1f - accuracy;
		f1_score = (2f*X)/(2f*X + Q + Z);
		f2_score = (5f*X)/(5f*X + 4f*Z + Q);

		f_score =  2f*sensitivity*specificity/(sensitivity + specificity);


		MCC = (float) ((X*Y) - (Q*Z)/Math.sqrt(X + Q + X + Z + Y + Q + Y + Z));
		
		productivity_factor = sensitivity/((X + Q)/(X + Y + Q + Z));
	}
	

	public void addMetrics(float true_positive_X, float true_negative_Y, float false_positive_Q, float false_negative_Z) {
		
		float X = true_positive_X;
		float Y = true_negative_Y;
		float Q = false_positive_Q;
		float Z = false_negative_Z;

		typeI_error = Q;
		typeII_error = Z;
		sensitivity = X/(X+Z);
		specificity = Y/(Q+Y);
		accuracy = (X + Y)/(X + Y + Q + Z);
		error_rate = 1f - accuracy;
		f1_score = (2f*X)/(2f*X + Q + Z);
		f2_score = (5f*X)/(5f*X + 4f*Z + Q);
		MCC = (float) ((X*Y) - (Q*Z)/Math.sqrt(X + Q + X + Z + Y + Q + Y + Z));
		productivity_factor = sensitivity/((X + Q)/(X + Y + Q + Z));
		
		accuracyList.add(accuracy);
		typeIList.add(typeI_error);		
		typeIIList.add(typeII_error);
		f1List.add(f1_score);
		
	}
	

	public void computeMeans() {
		
		meanAcc = 0f;
		meanTypeI = 0f;
		meanTypeII = 0f;
		meanF1 = 0f;
		
		for(int i = 0; i < accuracyList.size(); i++) {
			
			meanAcc += accuracyList.get(i);
			meanTypeI += typeIList.get(i);
			meanTypeII += typeIIList.get(i);
			meanF1 += f1List.get(i);
		}
		meanAcc = meanAcc/accuracyList.size();
		meanTypeI = meanTypeI/accuracyList.size();
		meanTypeII = meanTypeII/accuracyList.size();
		meanF1 = meanF1/accuracyList.size();
	}
	

	public float getMeanAccuracy() {
		return meanAcc;
	}
	
	public float getMeanTypeI() {
		return meanTypeI;
	}

	public float getMeanTypeII() {
		return meanTypeII;
	}
	
	public float getMeanFI() {
		return meanF1;
	}
	
	
	public float getSensitivity() {
		return sensitivity;
	}



	public void setSensitivity(float sensitivity) {
		this.sensitivity = sensitivity;
	}



	public float getSpecificity() {
		return specificity;
	}



	public void setSpecificity(float specificity) {
		this.specificity = specificity;
	}



	public float getAccuracy() {
		return accuracy;
	}



	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}



	public float getError_rate() {
		return error_rate;
	}



	public void setError_rate(float error_rate) {
		this.error_rate = error_rate;
	}



	public float getF1_score() {
		return f1_score;
	}



	public void setF1_score(float f1_score) {
		this.f1_score = f1_score;
	}



	public float getF2_score() {
		return f2_score;
	}



	public void setF2_score(float f2_score) {
		this.f2_score = f2_score;
	}



	public float getMCC() {
		return MCC;
	}



	public void setMCC(float mCC) {
		MCC = mCC;
	}



	public float getTypeI_error() {
		return typeI_error;
	}



	public void setTypeI_error(float typeI_error) {
		this.typeI_error = typeI_error;
	}



	public float getTypeII_error() {
		return typeII_error;
	}



	public void setTypeII_error(float typeII_error) {
		this.typeII_error = typeII_error;
	}
	
	public float getProductivity() {
		return productivity_factor;
	}
	
	@Override
	public 
	String toString() {
		String ref = accuracy + " " + productivity_factor;
		return ref;
	}

	public float getF_score() {
		return f_score;
	}

	public void setF_score(float f_score) {
		this.f_score = f_score;
	}
	
}
