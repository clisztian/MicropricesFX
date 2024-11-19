package tsetlin;

import com.csvreader.CsvReader;

import java.io.FileReader;
import java.io.IOException;



/**
 * Encoding interface for producing "convolutional" patches of the data
 * 
 * This is a generalized encoding that can work for all types of data, to 
 * encode virtually any type of feature
 * 
 * Typical use cases are:
 * 
 * 1) Standard tabular (row-column) data (dim_x = 1, dim_y = 1, dim_z = nfeatures, patch_x = 1, patch_y = 1
 * 2) Images (dim_x = width, dim_y = height, patch_x = conv_size_x, patch_y = conv_size_y
 * 3) Time Series (dim_x = n_features (including time stamp info), dim_y = time_window size (fixed), dim_z = 1, patch_x = conv_size_x, patch_y = 1 
 * 
 * @author lisztian
 *
 */
public class ConvolutionEncoder {

	/*
	 * Dimensions of input data 
	 */
	private int dim_x;
	private int dim_y;
	private int dim_z;
	
	/*
	 * Dimensions of convolutional patches 
	 */
	private int patch_dim_x;
	private int patch_dim_y;
	private int nbits;
	
	
	/*
	 * Computed feature dimensions
	 */
	int append_negated;
	private int number_of_features;
	private int number_of_patches;
	private int number_of_ta_chunks;
	private int[] y_train;
	private int[][] X_encoded_original;
	
	
	

	/**
	 * dim_x is the molecule + experiment encoded features of a given experiment
	 * dim_y is the time dimension representing a few dozen days of experiments
	 * 
	 * patch_dim_y is the convolution size
	 * 
	 * Construct a hierarchical encoder with the given number of bits in the X dimension
	 * The Z dimension is fixed to one
	 * 
	 * This encoding mechanism is ideal for one-dimensional time series, where X is the "feature"
	 * and Y dimension is the time dimension
	 *
	 * @param dim_x
	 * @param dim_y
	 * @param patch_dim_y
	 */
	public ConvolutionEncoder(int dim_x, int dim_y, int patch_dim_y) {
		
		this.nbits = dim_x;		
		this.dim_x = dim_x; 
		this.dim_y = dim_y;
		this.dim_z = 1;
		this.patch_dim_x = dim_x;
		this.patch_dim_y = patch_dim_y;
		
		number_of_features = patch_dim_x * patch_dim_y * dim_z + (dim_x - patch_dim_x) + (dim_y - patch_dim_y);
		number_of_patches = (dim_x - patch_dim_x + 1) * (dim_y - patch_dim_y + 1);
		number_of_ta_chunks = (((2*number_of_features-1)/32 + 1));
		
		this.append_negated = 1;
	}
	


	
	/**
	 * The generalized Hierarchical Encoder, used for multilayer images or mulitvariate timeseries
	 * @param dim_x
	 * @param dim_y
	 * @param dim_z
	 * @param patch_dim_x
	 * @param patch_dim_y
	 */
	public ConvolutionEncoder(int dim_x, int dim_y, int dim_z, int patch_dim_x, int patch_dim_y) {
		
		this.dim_x = dim_x; 
		this.dim_y = dim_y;
		this.dim_z = dim_z;
		this.patch_dim_x = patch_dim_x;
		this.patch_dim_y = patch_dim_y;
		
		number_of_features = patch_dim_x * patch_dim_y * dim_z + (dim_x - patch_dim_x) + (dim_y - patch_dim_y);
		number_of_patches = (dim_x - patch_dim_x + 1) * (dim_y - patch_dim_y + 1);
		number_of_ta_chunks = (((2*number_of_features-1)/32 + 1));
		
		append_negated = 1;
		
	}
	
	/**
	 * Formulation for standard tabular data
	 * @param z
	 */
	public ConvolutionEncoder(int x) {
		
		this.dim_x = x; 
		this.dim_y = 1;
		this.dim_z = 1;
		this.patch_dim_x = x;
		this.patch_dim_y = 1;
		
		number_of_features = patch_dim_x * patch_dim_y * dim_z + (dim_x - patch_dim_x) + (dim_y - patch_dim_y);
		number_of_patches = (dim_x - patch_dim_x + 1) * (dim_y - patch_dim_y + 1);
		number_of_ta_chunks = (((2*number_of_features-1)/32 + 1));
		
		append_negated = 1;
	}
	

	/**
	 * One sample where on output, the input data has now been transformed into separate features 
	 * that make up the inputs. They are also encoded into 32-bit chunks 
	 * @param X
	 * @return
	 */
	public int[] bit_encode(int[] X) {
		
		
		int[] encoded_X = new int[number_of_patches*number_of_ta_chunks];
		
		int patch_nr = 0;
		int encoded_pos = 0;

		// Produce the patches of the current image
		for (int y = 0; y < dim_y - patch_dim_y + 1; ++y) {
			for (int x = 0; x < dim_x - patch_dim_x + 1; ++x) {


				// Encode y coordinate of patch into feature vector 
				for (int y_threshold = 0; y_threshold < dim_y - patch_dim_y; ++y_threshold) {
					int patch_pos = y_threshold;

					if (y > y_threshold) {
						int chunk_nr = patch_pos / 32;
						int chunk_pos = patch_pos % 32;
						encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
					} else  {
						int chunk_nr = (patch_pos + number_of_features) / 32;
						int chunk_pos = (patch_pos + number_of_features) % 32;
						encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
					}
				}

				// Encode x coordinate of patch into feature vector
				for (int x_threshold = 0; x_threshold < dim_x - patch_dim_x; ++x_threshold) {
					int patch_pos = (dim_y - patch_dim_y) + x_threshold;

					if (x > x_threshold) {
						int chunk_nr = patch_pos / 32;
						int chunk_pos = patch_pos % 32;

						encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
					} else  {
						int chunk_nr = (patch_pos + number_of_features) / 32;
						int chunk_pos = (patch_pos + number_of_features) % 32;
						encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
					}
				} 

				// Encode patch content into feature vector
				for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
					for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
						for (int z = 0; z < dim_z; ++z) {
							int image_pos = (y + p_y)*dim_x*dim_z + (x + p_x)*dim_z + z;
							int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;

							if (X[image_pos] == 1) {
								int chunk_nr = patch_pos / 32;
								int chunk_pos = patch_pos % 32;
								encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
							} else  {
								int chunk_nr = (patch_pos + number_of_features) / 32;
								int chunk_pos = (patch_pos + number_of_features) % 32;
								encoded_X[encoded_pos + chunk_nr] |= (1 << chunk_pos);
							}
						}
					}
				}
				encoded_pos += number_of_ta_chunks;
				patch_nr++;
			}
		}
		return encoded_X;
		
	}
	
	

	/**
	 * Given the encoding parameters, will take a collection of n_samples of inputs, 
	 * and encode all of them into a the hierarchical form
	 * @param X
	 * @return encoded features
	 */
	public int[][] bit_encode_samples(int[][] X) {
		
		int n_samples = X.length;		
		int[][] encoded_X = new int[n_samples][number_of_patches*number_of_ta_chunks];
		int patch_nr = 0;
		int encoded_pos = 0;

		for(int i = 0; i < n_samples; i++) {
			
			encoded_pos = 0;
			// Produce the patches of the current image
			for (int y = 0; y < dim_y - patch_dim_y + 1; ++y) {
				for (int x = 0; x < dim_x - patch_dim_x + 1; ++x) {


					// Encode y coordinate of patch into feature vector 
					for (int y_threshold = 0; y_threshold < dim_y - patch_dim_y; ++y_threshold) {
						int patch_pos = y_threshold;

						if (y > y_threshold) {
							int chunk_nr = patch_pos / 32;
							int chunk_pos = patch_pos % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						} else  {
							int chunk_nr = (patch_pos + number_of_features) / 32;
							int chunk_pos = (patch_pos + number_of_features) % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						}
					}

					// Encode x coordinate of patch into feature vector
					for (int x_threshold = 0; x_threshold < dim_x - patch_dim_x; ++x_threshold) {
						int patch_pos = (dim_y - patch_dim_y) + x_threshold;

						if (x > x_threshold) {
							int chunk_nr = patch_pos / 32;
							int chunk_pos = patch_pos % 32;

							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						} else  {
							int chunk_nr = (patch_pos + number_of_features) / 32;
							int chunk_pos = (patch_pos + number_of_features) % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						}
					} 

					// Encode patch content into feature vector
					for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
						for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
							for (int z = 0; z < dim_z; ++z) {
								int image_pos = (y + p_y)*dim_x*dim_z + (x + p_x)*dim_z + z;
								int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;

								if (X[i][image_pos] == 1) {
									int chunk_nr = patch_pos / 32;
									int chunk_pos = patch_pos % 32;
									encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
								} else  {
									int chunk_nr = (patch_pos + number_of_features) / 32;
									int chunk_pos = (patch_pos + number_of_features) % 32;
									encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
								}
							}
						}
					}
					encoded_pos += number_of_ta_chunks;
					patch_nr++;
				}
			}
		}

		return encoded_X;
		
	}
	


	public int[][] bit_encoder(int n_samples, String fileName, char deliminator) throws IOException {
		
		CsvReader mnistFeed = new CsvReader(new FileReader(fileName));
		boolean read = mnistFeed.readHeaders();
		String[] record = mnistFeed.getRawRecord().split("[" + deliminator + "]+");
		
		
		int[][] encoded_X = new int[n_samples][number_of_patches*number_of_ta_chunks];
		y_train = new int[n_samples];
		int encoded_pos = 0;

		for(int i = 0; i < n_samples; i++) {
			
			mnistFeed.readRecord(); //grab a sample
			record = mnistFeed.getRawRecord().split("[" + deliminator + "]+");
			int patch_nr = 0;
			encoded_pos = 0;
			// Produce the patches of the current image
			for (int y = 0; y < dim_y - patch_dim_y + 1; ++y) {
				for (int x = 0; x < dim_x - patch_dim_x + 1; ++x) {


					// Encode y coordinate of patch into feature vector 
					for (int y_threshold = 0; y_threshold < dim_y - patch_dim_y; ++y_threshold) {
						int patch_pos = y_threshold;

						if (y > y_threshold) {
							int chunk_nr = patch_pos / 32;
							int chunk_pos = patch_pos % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						} else  {
							int chunk_nr = (patch_pos + number_of_features) / 32;
							int chunk_pos = (patch_pos + number_of_features) % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						}
					}

					// Encode x coordinate of patch into feature vector
					for (int x_threshold = 0; x_threshold < dim_x - patch_dim_x; ++x_threshold) {
						int patch_pos = (dim_y - patch_dim_y) + x_threshold;

						if (x > x_threshold) {
							int chunk_nr = patch_pos / 32;
							int chunk_pos = patch_pos % 32;

							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						} else  {
							int chunk_nr = (patch_pos + number_of_features) / 32;
							int chunk_pos = (patch_pos + number_of_features) % 32;
							encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
						}
					} 

					// Encode patch content into feature vector
					for (int p_y = 0; p_y < patch_dim_y; ++p_y) {
						for (int p_x = 0; p_x < patch_dim_x; ++p_x) {
							for (int z = 0; z < dim_z; ++z) {
								int image_pos = (y + p_y)*dim_x*dim_z + (x + p_x)*dim_z + z;
								int patch_pos = (dim_y - patch_dim_y) + (dim_x - patch_dim_x) + p_y * patch_dim_x * dim_z + p_x * dim_z + z;

								if (Integer.parseInt(record[image_pos]) == 1) {
									int chunk_nr = patch_pos / 32;
									int chunk_pos = patch_pos % 32;
									encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
								} else  {
									int chunk_nr = (patch_pos + number_of_features) / 32;
									int chunk_pos = (patch_pos + number_of_features) % 32;
									encoded_X[i][encoded_pos + chunk_nr] |= (1 << chunk_pos);
								}
							}
						}
					}
					encoded_pos += number_of_ta_chunks;
					patch_nr++;
				}
			}
			y_train[i] = Integer.parseInt(record[record.length - 1]);
		}
		mnistFeed.close();
		return encoded_X;
		
	}	


	
	
	/**
	 * Decodes the output of the local_inpretability which 
	 * @param local_interp
	 * @return
	 */
	public int[] decodeInterpretablePrediction(int[] local_interp) {
		
		
		
		
		return local_interp;		
	}
	
	
	
	/**
	 * Returns the number of features 
	 * This is the number of elements in a (x,y) patch 
	 * times dim_z plus location information of the patch
	 * @return number_of_features
	 */
	public int getNumber_of_features() {
		return number_of_features;
	}
	
	/**
	 * Returns the number of total patches
	 * @return number_of_patches
	 */
	public int getNumber_of_patches() {
		return number_of_patches;
	}
	
	/**
	 * Returns the number of chunks involved 
	 * in encoding the features into 
	 * 32 bits
	 * @return
	 */
	public int getNumber_of_ta_chunks() {
		return number_of_ta_chunks;
	}
	
	public int[] getLabels() {
		return y_train;
	}

	public int getNbits() {
		return nbits;
	}

	public void setNbits(int nbits) {
		this.nbits = nbits;
	}

	public int getDimX() {
		return dim_x;
	}
	
	public int getDimY() {
		return dim_y;
	}
	
	public int getDimZ() {
		return dim_z;
	}
	
	public int getDimPatchX() {
		return patch_dim_x;
	}
	
	public int getDimPatchY() {
		return patch_dim_y;
	}
	
	
	/**
	 * Get the ith convolution sample. Returns the 
	 * convolution form of the sample
	 * @param i
	 * @return
	 */
	public int[] getConvolutionEncodedSample(int i) {
		
		if(X_encoded_original == null) {
			return null;
		}
		
		return bit_encode(X_encoded_original[i]);		
	}
	
	

	
	
	public static void main(String[] args) throws Exception {

		int dim_x = 20;
		int dim_y = 1200;
		int patch_dim_y = 40;
		int n_samples = 200;
		
		ConvolutionEncoder encoder = new ConvolutionEncoder(dim_x, dim_y, patch_dim_y);			
		
		
	}

	public int[][] getX_encoded_original() {
		return X_encoded_original;
	}

	public void setX_encoded_original(int[][] x_encoded_original) {
		X_encoded_original = x_encoded_original;
	}
}
