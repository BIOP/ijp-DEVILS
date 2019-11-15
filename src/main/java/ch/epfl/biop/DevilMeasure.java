package ch.epfl.biop;

import java.util.Arrays;

public class DevilMeasure {
	
	/*
	 * DEVIL Measure
	 * 
	 * this class is used to store measurements made on plane(s) during DEVIL processing.
	 * Indeed, DEVIL can be "optimized", by giving values for the maxNorm, minFinal and maxFinal of each channel
	 * 
	 * At the end of the processing, the suggested parameters are printed within the Log
	 * 
	 */
	
	int nSeries;
	int nChannel;
	int nSlice;
	int nFrame;
	int totalPlane ;
	
	//double[][][][] maxNorm_da	;
	double[][][][] minFinal_da 	;
	double[][][][] maxFinal_da	;
	
	/*
	 * constructor defines variable
	 */
	public DevilMeasure(DevilParam dp) {
		
		this.nSeries 	= dp.getnSeries() ;
		this.nChannel 	= dp.getnChannel();
		this.nSlice 	= dp.getnSlice() ;
		this.nFrame 	= dp.getnFrame() ;
		
		totalPlane = nSeries*nChannel*nSlice*nFrame;
		
		//maxNorm_da	= new double[nSlice][nChannel][nFrame][nSeries];
		minFinal_da	= new double[nSlice][nChannel][nFrame][nSeries];
		maxFinal_da	= new double[nSlice][nChannel][nFrame][nSeries];
		
	}
	/*
	public void setMaxNorm(int[] position_ZCTS, double value) {
		maxNorm_da[ position_ZCTS[0] ][position_ZCTS[1]][position_ZCTS[2]][position_ZCTS[3]] = value ;
	}
	
	public double getMaxNormOfChannel(int channel){
		double maxValue = 0;
		
		// maxNorm_da [nSlice][nChannel][nFrame][nSeries]
		for (int zi = 0 ; zi < nSlice ;zi++){
			for (int ti = 0 ;ti < nFrame ;ti++){
				for (int si = 0 ; si < nSeries ;si++){
					if (maxNorm_da [zi][channel][ti][si] > maxValue){
						maxValue = maxNorm_da [zi][channel][ti][si];
					}
				}
			}
		}
		return maxValue;
	}
	*/
	
	public void setMinFinal(int[] position_ZCTS, double value) {
		minFinal_da[ position_ZCTS[0] ][position_ZCTS[1]][position_ZCTS[2]][position_ZCTS[3]] = value ;
	}
	
	public double getMinFinalOfChannel(int channel){
		double minValue = 10^24;
		
		// maxNorm_da [nSlice][nChannel][nFrame][nSeries]
		for (int zi = 0 ; zi < nSlice ;zi++){
			for (int ti = 0 ;ti < nFrame ;ti++){
				for (int si = 0 ; si < nSeries ;si++){
					if (minFinal_da [zi][channel][ti][si] < minValue){
						minValue = minFinal_da [zi][channel][ti][si];
					}
				}
			}
		}
		return minValue;
	}
	
	
	public void setMaxFinal(int[] position_ZCTS, double value) {
		maxFinal_da[ position_ZCTS[0] ][position_ZCTS[1]][position_ZCTS[2]][position_ZCTS[3]] = value ;
	}
	
	public double getMaxFinalOfChannel(int channel){
		double maxValue = 0;
		
		// maxNorm_da [nSlice][nChannel][nFrame][nSeries]
		for (int zi = 0 ; zi < nSlice ;zi++){
			for (int ti = 0 ;ti < nFrame ;ti++){
				for (int si = 0 ; si < nSeries ;si++){
					if (maxFinal_da [zi][channel][ti][si] > maxValue){
						maxValue = maxFinal_da [zi][channel][ti][si];
					}
				}
			}
		}
		return maxValue;
	}
	
	
	public void logMeasure(){
		ij.IJ.log("--------------------------------------------------------");
       	for  (int i=0 ; i < this.nChannel ; i++){
       		/*
       		double maxOfChannel_d	= getMaxNormOfChannel( i );
        	String maxOfChannel_str = new Double(maxOfChannel_d).toString(); 
       		ij.IJ.log("Channel"+(i+1)+" : maximum value of blurred image:"+maxOfChannel_str);
       		*/
       		double minFinal_d	= getMinFinalOfChannel( i );
        	String minFinal_str = new Double(minFinal_d).toString(); 
       		ij.IJ.log("Channel"+(i+1)+" : minimum value of final :"+minFinal_str);
       		
       		double maxFinal_d	= getMaxFinalOfChannel( i );
        	String maxFinal_str = new Double(maxFinal_d).toString(); 
       		ij.IJ.log("Channel"+(i+1)+" : maximum value of final:"+maxFinal_str);
       		ij.IJ.log("--------------------------------------------------------");
     	}
	}	
	
/*
 *  
 */
	
	public double[] reduceArray(double[] an_array, int start, int increment){
		int sub_array_size =  (an_array.length)/increment;
		double[] sub_array = new double[sub_array_size];
		
		for (int i = start ; i < sub_array_size ; i++ ){
			sub_array[i] = an_array[i+increment];
			//String i_str = new Integer(i+increment).toString(); 
			//String maxOfChannel_str = new Double(sub_array[i]).toString(); 
			//ij.IJ.log("maxOfChannel"+(start+1)+" : an_array["+i_str+"] = "+maxOfChannel_str);
		}		
		return sub_array ; 
	}
	
	
	public double[] sliceArray(double[] an_array, int start, int end){
		int sliced_array_size =  start - end + 1 ;
		double[] sliced_array = new double[sliced_array_size];
		
		for (int i = start ; i < end ; i++ ){
			sliced_array[i] = an_array[i];
			//String i_str = new Integer(i+increment).toString(); 
			//String maxOfChannel_str = new Double(sub_array[i]).toString(); 
			//ij.IJ.log("maxOfChannel"+(start+1)+" : an_array["+i_str+"] = "+maxOfChannel_str);
		}
		
		return sliced_array ; 
	}
	
	
	public double getMinOf(double[] an_array){
		
		double min = Arrays.stream(an_array).min().getAsDouble(); 
		// String minOfChannel_str = new Double(min).toString(); 
		//	ij.IJ.log("minOfChannel is devilMeasure: "+minOfChannel_str);
       	
		return min;
	}

	public double getMaxOf(double[] an_array){
		
		double max = Arrays.stream(an_array).max().getAsDouble(); 
		// String maxOfChannel_str = new Double(max).toString(); 
       	// ij.IJ.log("maxOfChannel is devilMeasure: "+maxOfChannel_str);
       	
		return max;
	}
	

	
	
	
}

