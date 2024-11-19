package dynamics;

import util.int2;

import java.io.Serializable;
import java.util.Arrays;


public class Histories implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A list of the histories
	 */
	private int[][] histories;
	
	int numInputLayers;
	int temporalHorizon;
	int numActivations;
	
	
	public Histories(int temporalHorizon, int numActivations) {
		
		this.numInputLayers = 1;
		this.temporalHorizon = temporalHorizon;
		this.numActivations = numActivations;
		
		histories = new int[temporalHorizon][numActivations];
	}

	public Histories(int2[] inputSizes, int temporalHorizon) {
		
		this.numInputLayers = inputSizes.length;
		this.temporalHorizon = temporalHorizon;
		
		histories = new int[temporalHorizon*numInputLayers][];
		for(int t = 0; t < temporalHorizon; t++) {
			for (int in = 0; in < numInputLayers; in++) {
	            histories[t + temporalHorizon * in] = new int[inputSizes[in].x*inputSizes[in].y];
		    }
		}		
	}



	/**
	 * Return all the histories
	 * @return
	 */
	public int[][] getHistories() {
		return histories;
	}
	
	/**
	 * Add a new history to the list
	 * @param h
	 * @throws Exception 
	 */
	public void addHistory(int[][] h) throws Exception {
		
		if(numInputLayers != h.length) {
			throw new Exception("Dimensions not equal");
		}
			
			
	        for (int t = temporalHorizon - 1; t > 0; t--) {
	            for (int in = 0; in < numInputLayers; in++) {
	            		            	
	            	System.arraycopy(histories[(t - 1) + temporalHorizon * in], 0, histories[t + temporalHorizon * in], 0, h[in].length);
	            }   
	        }

	        for (int in = 0; in < numInputLayers; in++) {
	        	System.arraycopy(h[in], 0, histories[0 + temporalHorizon * in], 0, h[in].length);
	        }	
	}
	
	public void addHistory(int[] h) {
		
		for (int t = temporalHorizon - 1; t > 0; t--) {
			System.arraycopy(histories[t - 1], 0, histories[t], 0, h.length);
		}
		System.arraycopy(h, 0, histories[0], 0, h.length);
	}
	
	
	/**
	 * Resets the history 
	 */
	public void resetHistory() {
		
		for(int i = 0; i < histories.length; i++) {
			Arrays.fill(histories[i],0);
		}
	}

	
	
	/**
	 * Gets the ith history
	 * @param i
	 * @return int[]
	 */
	public int[] getHistory(int i) {
		return histories[i];		
	}

	
	public void printHistories() {
		
		for(int i = 0; i < histories.length; i++) {
		  for(int j = 0; j < histories[i].length; j++) {
			  System.out.print(histories[i][j] + " ");
		  }
		  System.out.println("");
		}
		System.out.println("");
	}

	public int getTemporalHorizon() {
		return temporalHorizon;
	}
	
}
