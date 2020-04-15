package ch.epfl.biop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import ij.ImagePlus ;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.Blitter;
import ij.process.ImageProcessor;


import loci.formats.FormatException;

// here is the logic of the plugin

public class DEVILS {

	/*
	 * THE runner(s) on an ImageProcessor , AUTO mode or Advanced mode (defined in DevilParam )
	 */
	public static ImageProcessor DEVIL_ipr(DevilParam dp, DevilMeasure dm,int[] ZCTS_indexes,ImageProcessor ipr ) {
		// from DevilParam we retrieve some parameters
		
		// We want to retrieve current channel parameters 
		// in ZCTS_indexes ( Z[0],C[1],T[2],S[3] ),
		
		// in previous version maxBlur was a parameter
		// simplify worflow by fixing it to 65535
		//float maxBlur	= dp.getChannelMaxBlur(ZCTS_indexes[1])  ; 
		float maxBlur	= dp.default_maxBlur  ; 
		
		float min_final = dp.getChannelMinFinal(ZCTS_indexes[1]) ;
		float max_final = dp.getChannelMaxFinal(ZCTS_indexes[1]) ;
		
		// in previous version particle_size was the same for all the channels
		//int particle_size = dp.objectSize ;
		float particle_size = dp.getChannelObjectSize(ZCTS_indexes[1]) ;
		float blur_size = 2* particle_size ;
		
		/*
		 * DEVIL is a very simple workflow that comprises :
		 * - divide the image by a blurred version ( sigma = 2 * objectSize)
		 * - make the sqrt
		 * - apply a rollingBallBackground ( radius = objectSize)
		 * 
		 * NB : some measurement are made during the process and stored in DevilMeasure
		 */
		
		
		/* IF we want to add scale down in the workflow 
		 * 		  
		 * resized = ip.resize(targetWidth,targetHeight);
		 */
		
		// convert ipr to 32-bit and duplicate to process
		ImageProcessor ipr_ori_32 	= ipr.convertToFloatProcessor();
		ImageProcessor ipr_blur_32 	= ipr_ori_32.duplicate();
		
		// blur and normalize it
		ipr_blur_32.blurGaussian( blur_size);
		
		// Measure the max of the Blurred Image and store the value in the DevilMeasure
		// so we can output the value later on
		//double maxBlur_measured = ipr_blur_32.getStats().max;
		//dm.setMaxNorm(ZCTS_indexes, maxBlur_measured);
		
		//String plane_index_str	= new Integer(plane_index).toString(); 
		//String maxOfChannel_str = new Double(maxBlur_measured).toString(); 
       	//ij.IJ.log("@"+plane_index_str+" maxOfChannel ipr : "+maxOfChannel_str);
		
		ipr_blur_32.multiply( 1.0 / maxBlur );
		
		/* using a imagePlus
		ImageCalculator ipl_c = new ImageCalculator();
		ImagePlus ipl_corr_32 = ipl_c.run("Divide create", new ImagePlus("",ipr_ori_32), new ImagePlus("",ipr_blur_32) );
		ImageProcessor ipr_corr_32 = ipl_corr_32.getProcessor();
		*/
		
		// Fortunately, Oli found a way to divide an ipr by another ipr without using an ImagePlus intermediate !
		ipr_ori_32.copyBits(ipr_blur_32, 0, 0, Blitter.DIVIDE);
		// do sqrt
		ipr_ori_32.sqrt();
		// and Background substraction
		BackgroundSubtracter bkgdSub = new BackgroundSubtracter();		
		bkgdSub.rollingBallBackground(ipr_ori_32,particle_size,false,false,false,true,false);
	
		// Measure the min and max of the final image and store the values in the DevilMeasure
		// so we can output the value later on
		double minFinal_measured = ipr_ori_32.getStats().min;
		dm.setMinFinal(ZCTS_indexes, minFinal_measured);
		double maxFinal_measured = ipr_ori_32.getStats().max;
		dm.setMaxFinal(ZCTS_indexes, maxFinal_measured);
		
		if ( dp.getOutputBitDepth().equals("16-bit") ) {
			// set min and max to the defined values
			ipr_ori_32.setMinAndMax(min_final, max_final);
			// convert to 16-bit
			ImageProcessor ipr_corr_16 = ipr_ori_32.convertToShort(true);
			// return DEVILed ipr
			return ipr_corr_16;			
		} else if ( dp.getOutputBitDepth().equals("8-bit") ) {
			// set min and max to the defined values
			ipr_ori_32.setMinAndMax(min_final, max_final);
			// convert to 8-bit
			ImageProcessor ipr_corr_8 = ipr_ori_32.convertToByte(true);
			return ipr_corr_8;
		} else {
			return ipr_ori_32;
		}
	}
	
	
	/*
	 *  PARALLEL PROCESSING
	 *  - DEVIL_ipr(...)
	 *  - export multi .tif files
	 *  
	 *  inspired by : http://albert.rierol.net/imagej_programming_tutorials.html
	 *  
	 */
	
	private final static int n_cpus=Runtime.getRuntime().availableProcessors();
	public static final String Devils_Parameter_Filename = "DevilsParameters.json";
	
	
	/*
	 * Here we're using vStack to get image processor by their index.
	 * Each plan as a index depending of  Z C T Serie
	 * 
	 * vStack 
	 * imported from BIO-formats BFVirtualStack 
	 * is declared in DevilParam 
	 * 
	 */
	public static void run(DevilParam dp) throws IOException, FormatException {  
		
		// to output suggestions for parameters
		DevilMeasure dm = new DevilMeasure(dp) ;

		// Store Devils Parameters as json file
		// TODO : Q for Romain : should we support multiple files devilsed to a single folder ?
		// If yes the parameter file name should be different for each file, or it will be erased
		Gson gson = new Gson();
		gson.toJson(dp, new FileWriter(dp.getOutputDir() + File.separator + Devils_Parameter_Filename));

		for (int iSeries = 0 ; iSeries < dp.nSeries ; iSeries++ ){ // for each series of the selected file
        	// set the current Series
			dp.setCurrentSeries(iSeries);
        	
			// DECLARE A thread array of size Nbr of available CPU
			// final Thread[] threads = newThreadArray();  
	        final Thread[] threads = new Thread[n_cpus]; 
	        // initiate an atomicInteger
	        final AtomicInteger ai 		= new AtomicInteger(0); 
         
	       	for (int ithread = 0; ithread < threads.length; ithread++) {  
	  
	            // Concurrently run in as many threads as CPUs  
	             threads[ithread] = new Thread() {  
	                          
	                { setPriority(Thread.NORM_PRIORITY); }  
	  
	                public void run() {  
	  
		                // Each thread processes a few items in the total list  
		                // Each loop iteration within the run method  
		                // has a unique 'i' number to work with  
		                // and to use as index in the results array:  
	                	
	                	
	                	for (int i = ai.getAndIncrement(); i < dp.perSeriesPlanesNbr; i = ai.getAndIncrement() ) {  
	                		
		                   	int[] 	ZCT_indexes = dp.ch_separator.getZCTCoords(i);
		                	int 	currentSerie = dp.ch_separator.getSeries();
		                   	int[]	ZCTS_indexes= { ZCT_indexes[0], ZCT_indexes[1],ZCT_indexes[2],currentSerie };
		                	int 	totalPlanesNbr = dp.getTotalPlanesNbr();
		                	// process each plane        					
	    					// get the imageProcessor at i+1 (because it starts at 1)		                	
		                					
							
			                	ImageProcessor currentPlane_ipr = dp.vStack.getProcessor(i+1);
			                	
		         				// and define the name accordingly using 
		        				String 	currentPlaneIndexes_str 	=  "-t"+ZCTS_indexes[2]+"-z"+ZCTS_indexes[0]+"-c"+ZCTS_indexes[1]+"-i"+i;
		    					
		        				// if the file have multiple series , add serie nbr
		        				if ( dp.getnSeries() > 1 ) currentPlaneIndexes_str 	=  "s"+ZCTS_indexes[3]+"-"+currentPlaneIndexes_str;   					
		        				
		        				// process the image processor
		    					ImageProcessor processed_currentPlane_ipr ;
		    					if (dp.advancedParam){
		    						//processed_currentPlane_ipr = DEVIL_ipr(dp,dm,ZCTS_indexes,currentPlane_ipr, max_norm_arrayF[ ZCT_indexes[1] ], min_final_arrayF[ ZCT_indexes[1] ], max_final_arrayF[ ZCT_indexes[1] ] );
		    						processed_currentPlane_ipr = DEVIL_ipr(dp,dm,ZCTS_indexes,currentPlane_ipr);
		        					
		    					}else{
		    						processed_currentPlane_ipr = DEVIL_ipr(dp,dm,ZCTS_indexes,currentPlane_ipr);
		    					}
 		  		
								// make ipl from ipr
		 						ImagePlus currentPlane_ipl_output 	= new ImagePlus(currentPlaneIndexes_str+"--processed"	, processed_currentPlane_ipr);
		 						
		 						Calibration cal = new Calibration(currentPlane_ipl_output) ;
		     					cal.setUnit("micron");
		     					cal.pixelWidth 	= dp.voxelSize[0];
		     					cal.pixelHeight	= dp.voxelSize[1];
		     					cal.pixelDepth	= dp.voxelSize[2]; //as no effect ! 
		     					currentPlane_ipl_output.setCalibration(cal);
		     					
		     					// Output
		     					String ouput_filePath 	= dp.getOutputPath();
		     					if (totalPlanesNbr > 1){
		     						ouput_filePath 	+= "_"+currentPlaneIndexes_str+".tif";
		     					}
		     					final FileSaver ipl_fileSaver 	= new FileSaver(currentPlane_ipl_output);
		     					ipl_fileSaver.saveAsTiff(ouput_filePath);
		     					
			               
	                	}
	                	
	                }
	            };  
	        }
        
       	// DO IT ! 
       	startAndJoin(threads);  
        }
       	// Final Log print with measured value 
       	dm.logMeasure();
            	
	} 
	
	/** Start all given threads and wait on each of them until all are done. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    public static void startAndJoin(Thread[] threads){  
        for (int ithread = 0; ithread < threads.length; ++ithread)  
        {  
            threads[ithread].setPriority(Thread.NORM_PRIORITY);  
            threads[ithread].start();  
        }  
  
        try  
        {     
            for (int ithread = 0; ithread < threads.length; ++ithread)  
                threads[ithread].join();  
        } catch (InterruptedException ie)  
        {  
            throw new RuntimeException(ie);  
        }  
    }

} // end of public class DEVIL 



