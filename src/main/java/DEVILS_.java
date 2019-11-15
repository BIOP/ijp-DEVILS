
import java.io.IOException;

import org.joda.time.DateTime;

import ch.epfl.biop.DEVILS;
import ch.epfl.biop.DevilParam;
import fiji.util.gui.GenericDialogPlus;

import ij.IJ;
import ij.ImageJ;
import ij.Prefs;
import ij.plugin.PlugIn;

import loci.formats.FormatException;

// here is the User Interface that runs the class DEVIL.java

public class DEVILS_ implements PlugIn {

	@Override
	public void run(String arg0) {
		String 	defaultPath 		= Prefs.get("ch.epfl.biop.devil.defaultPath"			, Prefs.getImageJDir());
		int 	objectSize 			= Prefs.getInt("ch.epfl.biop.devil.particle_size"		, 25 );
		boolean	advancedParam 		= Prefs.getBoolean("ch.epfl.biop.devil.advancedParam"	, false);
				
		GenericDialogPlus gd = new GenericDialogPlus("DEVILS parameters");
		
		gd.addFileField("Select_File", defaultPath);
		gd.addNumericField("Largest_object_size (in pixel)", objectSize, 0);
		gd.addCheckbox("Advanced_parameters", advancedParam);
		gd.showDialog();
		
		// if "canceled" send error and return
		if ( gd.wasCanceled() ){
			IJ.error("canceled by user");
			return;
		} 
		// otherwise, retrieve values
		defaultPath 		= 		gd.getNextString();
		objectSize 			= (int) gd.getNextNumber();
		advancedParam		= 		gd.getNextBoolean();
		
		// set choices in Prefs		
		Prefs.set("ch.epfl.biop.devil.defaultPath"		, defaultPath);
		Prefs.set("ch.epfl.biop.devil.particle_size"	, objectSize );
		Prefs.set("ch.epfl.biop.devil.advancedParam"	, advancedParam );
		
		//String maxNorm_string	= "Default";
		String minFinal_string	= "Default";
		String maxFinal_string	= "Default";
		String objectSize_string= String.valueOf(objectSize);
		Prefs.set("ch.epfl.biop.devil.objectSize"		, objectSize_string		);
		String outputBitDepth_string= "16-bit";
		String outputDir_string = "";
		
		// if advanced settings was selected
		if ( advancedParam ){
			//maxNorm_string			= Prefs.get("ch.epfl.biop.devil.max_norm"			, "65535, 65535, ..." 	);
			minFinal_string			= Prefs.get("ch.epfl.biop.devil.min_final"			, "-10, -10, ..." 		);
			maxFinal_string			= Prefs.get("ch.epfl.biop.devil.max_final"			, "1000, 1000, ..." 	);
			objectSize_string		= Prefs.get("ch.epfl.biop.devil.objectSize"			, "10, 10, ..." 		);
			outputBitDepth_string 	= Prefs.get("ch.epfl.biop.devil.outputBitDepth"		, "16-bit" 				);
			outputDir_string		= Prefs.get("ch.epfl.biop.devil.outputDir"			, "" 					);
			
			String[] outputBitDepthChoice = {"16-bit","32-bit"};
			
			GenericDialogPlus gd_adParam = new GenericDialogPlus("DEVILS advanced parameters");
			gd_adParam.addMessage("You can specify an ouput directory.\n(Leave the field empty to create a DEVILS subfolder)");
			gd_adParam.addDirectoryField("Output_directory", outputDir_string);
			
			gd_adParam.addMessage("You can specify values for each channel, separated by ',' .\n (*) non numerical values will be replaced by default");
			
			//gd_adParam.addStringField("Maximum (for normalization step)"	, maxNorm_string	,15 );
			gd_adParam.addStringField("Minimum (for final conversion step)"	, minFinal_string	,15	);
			gd_adParam.addStringField("Maximum (for final conversion step)"	, maxFinal_string	,15	);	
			gd_adParam.addStringField("Object Size (in pixel)"				, objectSize_string	,15	);
			gd_adParam.addMessage("----------------------------------------------------------------------------------------");
			gd_adParam.addChoice("Output_bit_depth", outputBitDepthChoice , outputBitDepth_string);
			gd_adParam.addMessage("*16-bit images will be lighter, but requires conversion\nand thus to specify Min. and Max. values for final conversion ");
			gd_adParam.addMessage("**32-bit images will be heavier, but not need for conversion\nneither to know Min. and Max. values for final conversion");
			
			gd_adParam.showDialog();
			
			if ( gd_adParam.wasCanceled() ){
				IJ.error("canceled by user");
				return;
			} 
			outputDir_string		= 		gd_adParam.getNextString();
			minFinal_string			= 		gd_adParam.getNextString();
			maxFinal_string 		= 		gd_adParam.getNextString();
			objectSize_string		= 		gd_adParam.getNextString();
			outputBitDepth_string 	=		gd_adParam.getNextChoice();
			
			Prefs.set("ch.epfl.biop.devil.outputDir"		, outputDir_string		);
			Prefs.set("ch.epfl.biop.devil.min_final"		, minFinal_string		);
			Prefs.set("ch.epfl.biop.devil.max_final"		, maxFinal_string		);
			Prefs.set("ch.epfl.biop.devil.objectSize"		, objectSize_string		);
			Prefs.set("ch.epfl.biop.devil.outputBitDepth"	, outputBitDepth_string	);
			
		}
		
		//DevilParam dp = new DevilParam(defaultPath, objectSize, advancedParam, maxNorm_string,minFinal_string,maxFinal_string, outputBitDepth_string);
		DevilParam dp = new DevilParam(defaultPath, outputDir_string, objectSize, advancedParam, minFinal_string,maxFinal_string,objectSize_string, outputBitDepth_string);
		
		dp.logParam();
     
		DateTime starter = DateTime.now();
        IJ.log("Starts at : "+ starter);
        long starter_ms = System.currentTimeMillis();
		
		try {
			DEVILS.run(dp);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		
		DateTime ender = DateTime.now();
		ij.IJ.log("Ends at : "+ ender);
	    long ender_ms = System.currentTimeMillis();
	    
	    ij.IJ.log("DEVILS consumed : "+ ( (ender_ms - starter_ms) / 1000 )+"sec of your life" );
		
	}
	
	
	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = DEVILS_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
		
	}
}
