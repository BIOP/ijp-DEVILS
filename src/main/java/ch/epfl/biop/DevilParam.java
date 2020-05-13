package ch.epfl.biop;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import ij.IJ;

import ij.ImagePlus;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.IFormatWriter;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.BFVirtualStack; 
import ome.units.quantity.Length;
import ome.xml.meta.MetadataRetrieve;

public class DevilParam {
	
	
	/*
	 * DEVIL param
	 * 
	 * this class is used to store many parameters required during DEVIL processing
	 * 
	 */
	public final float default_maxBlur 		= 65535	;
	public final float default_min_final	= -100	;
	public final float default_max_final	= 10000 	; 
	
	String defaultPath;
	
	int objectSize;
	
	boolean advancedParam;
	
	//String maxNorm_string;
	String minFinal_string;
	String maxFinal_string;
	String objectSize_string;
	String outputBitDepth_string ; 
	
	// to store parameters for each channels, we use arrays 
	//float[] maxBlur		; 
	float[] minFinal  			;
	float[] maxFinal  			;
	float[] objectSize_array	;
	
	
	/*
	 * Directories Params
	 */
	// input
	File 	file		;
	String 	imageName 	;
	String 	fileDir_str 	;
	// output
	File 	output_dir 	;
	String 	output_dir_str ;

	Map<String, Double> devilsMeasureLog; // to store min max values detected along serie, c, z, t -> serialized into DevilsParameters.json

	/*
	 * Image Param
	 */
	transient ChannelSeparator ch_separator; // Made transient to prevent serialization
	transient BFVirtualStack vStack; // Made transient to prevent serialization

	transient boolean littleEndian;
	
	int nSeries;
	int nChannel;
	int nSlice;
	int nFrame;
	int perSeriesPlanesNbr ;
	int totalPlanesNbr ;
	
	double[] voxelSize = {1,1,1};
	
	
	// initialized after GUI
	//public DevilParam(String defaultPath, int objectSize, boolean advancedParam, String max_norm_string, String min_final_string, String max_final_string, String outputBitDepth_string) {
	public DevilParam(String defaultPath,String outputDir, int objectSize, boolean advancedParam, String min_final_string, String max_final_string, String objectSize_string, String outputBitDepth_string) {
				
		this.defaultPath 		= defaultPath;
		this.output_dir_str 	= outputDir;
		
		this.objectSize 		= objectSize;
		this.advancedParam 		= advancedParam;
		
		//this.maxNorm_string 	= max_norm_string;
		this.minFinal_string 	= min_final_string;
		this.maxFinal_string 	= max_final_string;
		this.objectSize_string	= objectSize_string;
		
		this.outputBitDepth_string = outputBitDepth_string;
		
		initFilesAndFolder();
		
		initImage();
		
	}

	// Constructor for the previewer
	public DevilParam(ImagePlus imp, int objectSize, boolean advancedParam, String min_final_string, String max_final_string, String objectSize_string, String outputBitDepth_string) {

		this.defaultPath 		= "";//defaultPath;
		this.output_dir_str 	= "";//outputDir;

		this.objectSize 		= objectSize;
		this.advancedParam 		= advancedParam;

		//this.maxNorm_string 	= max_norm_string;
		this.minFinal_string 	= min_final_string;
		this.maxFinal_string 	= max_final_string;
		this.objectSize_string	= objectSize_string;

		this.outputBitDepth_string = outputBitDepth_string;

		//initFilesAndFolder();

		//initImage();

		this.littleEndian = true;//ch_separator.isLittleEndian();

		this.nSeries 			= 1;//ch_separator.getSeriesCount()	;
		this.nChannel			= imp.getNChannels();//ch_separator.getSizeC();
		this.nSlice				= imp.getNSlices();//ch_separator.getSizeZ();
		this.nFrame				= imp.getNFrames();//ch_separator.getSizeT();
		this.perSeriesPlanesNbr	= nChannel * nSlice * nFrame;
		this.totalPlanesNbr 	= nSeries * nChannel * nSlice * nFrame;

		//this.vStack = new BFVirtualStack(this.defaultPath, this.ch_separator, false, false, false);

		IJ.log( "nSeries "+String.valueOf(nSeries));
		IJ.log( "nChannel "+String.valueOf(nChannel));
		IJ.log( "nSlice "+String.valueOf(nSlice));
		IJ.log( "nFrame "+String.valueOf(nFrame));
		IJ.log( "totalPlanesNbr "+String.valueOf(totalPlanesNbr));

		// Now that we know file dimensions, we check that the parameters are in sufficient number (to cover all channels)
		//checkParamMaxNorm();
		checkParamMinFinal();
		checkParamMaxFinal();
		checkParamObjectSize();
		// Retrieve Calibrataion
		//final MetadataRetrieve retrieve = service.asRetrieve(ch_separator.getMetadataStore());

		// calibration
//		final String dimOrder = ch_separator.getDimensionOrder().toUpperCase();

//		final int posX = dimOrder.indexOf( 'X' );
		//Length calX = retrieve.getPixelsPhysicalSizeX( 0 );
		/*if ( posX >= 0 && calX != null && calX.value().doubleValue() != 0 )
			voxelSize[0] = calX.value().doubleValue();

		final int posY = dimOrder.indexOf( 'Y' );
		Length calY = retrieve.getPixelsPhysicalSizeY( 0 );
		if ( posY >= 0 && calY != null && calY.value().doubleValue() != 0 )
			voxelSize[1] = calY.value().doubleValue();

		final int posZ = dimOrder.indexOf( 'Z' );
		Length calZ = retrieve.getPixelsPhysicalSizeZ( 0 );
		if ( posZ >= 0 && calZ != null && calZ.value().doubleValue() != 0 )
			voxelSize[2] = calZ.value().doubleValue();
		//String voxel_depth = new Double(voxelSize[2]).toString();
		//ij.IJ.log(" voxel_depth : "+voxel_depth);*/

		voxelSize[0] = imp.getCalibration().pixelWidth;
		voxelSize[1] = imp.getCalibration().pixelHeight;
		voxelSize[2] = imp.getCalibration().pixelDepth;
	}
	
	/*
	 * initialize filepath and create output directories
	 */
	public void initFilesAndFolder(){
		this.file			= new File(defaultPath) ;
		this.imageName 		= file.getName();
		this.fileDir_str 	= file.getParent() + File.separator;
		
		if (output_dir_str.equals("") ){
			// prepare output
			this.output_dir 	= new File(fileDir_str+"DEVILS");
			output_dir.mkdir();
			//this.output_dir_str = output_dir.getAbsolutePath() + File.separator;
		} else {
			this.output_dir 	= new File(output_dir_str + File.separator);
			output_dir.mkdir();
			//this.output_dir_str = output_dir.getAbsolutePath() + File.separator;
		}
		this.output_dir_str = output_dir.getAbsolutePath() + File.separator;
		IJ.log(output_dir_str);
	}
	
	/*
	 * initialize image reader , get image infos
	 * 
	 * metadata reading adapted from ::  http://www.programcreek.com/java-api-examples/index.php?source_dir=Stitching-master/src/main/java/Stitch_Multiple_Series_File.java
	 * 
	 */
	public void initImage() {

		try {
		// MAKE a virtual stack from the defined files 
			// Create a ChannelSeparator to retrieve informations about the file
			ch_separator = new ChannelSeparator();
		
			final ServiceFactory factory = new ServiceFactory();
			final OMEXMLService service = factory.getInstance( OMEXMLService.class );
			final IMetadata meta = service.createOMEXMLMetadata();
			
			ch_separator.setMetadataStore( meta );
			//very important to setId AFTER MetadataStore, otherwise send non null issue ! 
			ch_separator.setId(defaultPath);
			this.littleEndian = ch_separator.isLittleEndian();
			
			this.nSeries 			= ch_separator.getSeriesCount()	;
			this.nChannel			= ch_separator.getSizeC();
			this.nSlice				= ch_separator.getSizeZ();
			this.nFrame				= ch_separator.getSizeT();
			this.perSeriesPlanesNbr	= nChannel * nSlice * nFrame;
			this.totalPlanesNbr 	= nSeries * nChannel * nSlice * nFrame;
			
			//this.vStack = new BFVirtualStack(this.defaultPath, this.ch_separator, false, false, false);
			
			IJ.log( "nSeries "+String.valueOf(nSeries));
			IJ.log( "nChannel "+String.valueOf(nChannel));
			IJ.log( "nSlice "+String.valueOf(nSlice));
			IJ.log( "nFrame "+String.valueOf(nFrame));
			IJ.log( "totalPlanesNbr "+String.valueOf(totalPlanesNbr));
			
			// Now that we know file dimensions, we check that the parameters are in sufficient number (to cover all channels)
			//checkParamMaxNorm();
			checkParamMinFinal();
			checkParamMaxFinal();
			checkParamObjectSize();
			// Retrieve Calibrataion 
			final MetadataRetrieve retrieve = service.asRetrieve(ch_separator.getMetadataStore());
			
			// calibration
			final String dimOrder = ch_separator.getDimensionOrder().toUpperCase();
			
			final int posX = dimOrder.indexOf( 'X' );
			Length calX = retrieve.getPixelsPhysicalSizeX( 0 );
			if ( posX >= 0 && calX != null && calX.value().doubleValue() != 0 )
				voxelSize[0] = calX.value().doubleValue();
			
			final int posY = dimOrder.indexOf( 'Y' );
			Length calY = retrieve.getPixelsPhysicalSizeY( 0 );
			if ( posY >= 0 && calY != null && calY.value().doubleValue() != 0 )
				voxelSize[1] = calY.value().doubleValue();
			
			final int posZ = dimOrder.indexOf( 'Z' );
			Length calZ = retrieve.getPixelsPhysicalSizeZ( 0 );
			if ( posZ >= 0 && calZ != null && calZ.value().doubleValue() != 0 )
				voxelSize[2] = calZ.value().doubleValue();
			//String voxel_depth = new Double(voxelSize[2]).toString(); 
	   		//ij.IJ.log(" voxel_depth : "+voxel_depth);
		
    	} catch (Exception e) {
			e.printStackTrace();
    	}
	}
	
	

	public void setCurrentSeries(int iSeries) {
		// TODO Auto-generated method stub
		ch_separator.setSeries(iSeries);
		IJ.log( "serieIndex "+iSeries);
		try {
			this.vStack = new BFVirtualStack(this.defaultPath,this.ch_separator, false, false, false);	
		} catch (Exception e) {
			e.printStackTrace();
    	}
	}
	
	public String getOutputPath(){
		return output_dir_str + imageName ;
	}

	public String getOutputDir(){
		return output_dir_str ;
	}
	
	public String getOutputBitDepth(){
		return outputBitDepth_string; 
	}
/*
	// call in initImage()
	public void setImageParams(int nSeries, int nChannel, int nSlice, int nFrame) {
		this.nSeries 	= nSeries	;
		this.nChannel	= nChannel	;
		this.nSlice		= nSlice	;
		this.nFrame		= nFrame	;
		this.totalPlanesNbr 	= nSeries * nChannel * nSlice * nFrame;
		// Now that we know file dimensions, we check that the parameters are in sufficient number (to cover all channels)
		checkParamMaxNorm();
		checkParamMinFinal();
		checkParamMaxFinal();
		
	}
*/
	// Image Param getters
	public int getnSeries() {
		return nSeries;
	}

	public int getnChannel() {
		return nChannel;
	}

	public int getnSlice() {
		return nSlice;
	}

	public int getnFrame() {
		return nFrame;
	}

	public int getTotalPlanesNbr() {
		return totalPlanesNbr;
	}
	/*
	 * DEALING WITH PARAMETERS	
	 */
	
	/*
	public void checkParamMaxNorm(){
		this.maxBlur = checkParam( maxNorm_string	, default_maxBlur);
	}
	
	
	public float getChannelMaxBlur(int i) {
		return maxBlur[i];
	}
	*/
	
	public void checkParamMinFinal(){
		this.minFinal = checkParam( minFinal_string	, default_min_final	);
	}
	
	private void checkParamObjectSize() {
		this.objectSize_array = checkParam( objectSize_string , this.objectSize	);		
	}
	
	public float getChannelObjectSize(int i) {
		return objectSize_array[i];
	}
	
	public float getChannelMinFinal(int i) {
		return minFinal[i];
	}

	public void checkParamMaxFinal(){
		this.maxFinal = checkParam( maxFinal_string	, default_max_final	);
	}
	public float getChannelMaxFinal(int i) {
		return maxFinal[i];
	}
	
	
	/*
	 * Check if there is enough parameters (respectively to the number of channel)
	 * if empty replace by default
	 * 
	 * requires function : stringToArrayFloat (String str , float defaultValue )
	 */
	public float[] checkParam( String param_to_check, float defaultValue) {
		boolean advancedMode	= this.advancedParam ; 
		float[] param_checked 	= new float[nChannel];
		
		if (advancedMode){ 
			//make the string an float[]
			float[] param_to_check_array = stringToArrayFloat( param_to_check, defaultValue);
			
			// check if size of the array correspond to number of channels
			if (param_to_check_array.length == nChannel){
				param_checked 	= param_to_check_array;
			}else{//otherwise replace by default value
				Arrays.fill(param_checked, defaultValue);
			}
		} else {// if automode, return a array filled with defaultValue
			Arrays.fill(param_checked, defaultValue);
			IJ.log("DEVIL default mode");
		}
				
		return param_checked;
	}
	
	/*
	 * Convert a string to an array of float
	 * if not a float, replace it by default
	 */
	public static float[] stringToArrayFloat (String str , float defaultValue ){
		String[] str_array		= str.split(",");
		float[] a_flot_array 	= new float[str_array.length];
		for (int ii = 0 ; ii < str_array.length ; ii++){
		    try {
		    	a_flot_array[ii] = Float.valueOf(str_array[ii].trim()).floatValue();	
		    } catch (NumberFormatException nfe){
		    	a_flot_array[ii] = defaultValue ;
		    	IJ.log("Ch"+(ii+1)+" : not an appropriate number : " + str_array[ii] +"replaced by "+defaultValue);
		    }
		    //IJ.log(ii+"-"+a_flot_array[ii]);
		}
		return a_flot_array;
	}

	public void logParam() {
		ij.IJ.log("Advanced parameters, defined by user :"+ defaultPath);
		ij.IJ.log("Defined ObjectSize (pixel) :"+objectSize_string);
		ij.IJ.log("Advanced parameters, defined by user");
		//ij.IJ.log("Channels, maximum value of blurred image :"+maxNorm_string);
		ij.IJ.log("Channels, minimum value of final :"+minFinal_string);
		ij.IJ.log("Channels, maximum value of final :"+maxFinal_string);
		ij.IJ.log("Output bit_depth :"+outputBitDepth_string);
	}


}
