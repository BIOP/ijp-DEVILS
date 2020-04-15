#@File(label="Select file to process") image_file
#@Integer(label="largest_object_size") largest_object_size

/**
 * Script which executes DEVILS plugin on a file and then exports the result as
 * - a XML / HDF5 multiresolution dataset
 */

IJ.log("-- Executing DEVILS Plugin");

IJ.run("DEVILS",  " browse="+image_file.getAbsolutePath()+
				  " select_file="+image_file.getAbsolutePath()+
				  " largest_object_size="+largest_object_size);

// TODO : Q 4 Romain : can the export folder be different ?
def devils_image_folder = image_file.getParent()+File.separator+"DEVILS"+File.separator
def devilsParam = (new Gson()).fromJson(new FileReader(devils_image_folder+DEVILS.Devils_Parameter_Filename), DevilParam.class)

IJ.log("-- Exporting As Xml-Hdf5 file format");

// One xml/hdf5 pair per series
(0..devilsParam.nSeries-1).each{
	// Open the serie as a virtual stack
	
	// and define the name accordingly using 
	file_name_filter = devilsParam.imageName

	if ( devilsParam.getnSeries() > 1 ) file_name_filter +=  "_s"+it		
	      				
	def impV = FolderOpener.open(devils_image_folder, "virtual file="+file_name_filter)

	println("file_name_filter:"+file_name_filter)
	println("devilsParam.nChannel:"+devilsParam.nChannel)
	println("devilsParam.nSlice:"+devilsParam.nSlice)
	println("devilsParam.nFrame:"+devilsParam.nFrame)

	//impV.show()

	// re-order using ch_count and total plane number
	def reordered_impV = HyperStackConverter.toHyperStack(impV, devilsParam.nChannel, devilsParam.nSlice, devilsParam.nFrame, "Composite")

	// set calibration
	reordered_impV.getCalibration().pixelWidth = devilsParam.voxelSize[0]
	reordered_impV.getCalibration().pixelHeight = devilsParam.voxelSize[1]
	reordered_impV.getCalibration().pixelDepth = devilsParam.voxelSize[2]

	reordered_impV.setTitle(file_name_filter)
	
	reordered_impV.show()
	// reordered_impV.show()
	// TODO : check and do differently when 32 bits
	IJ.run("Export Current Image as XML/HDF5", " value_range=[Use values specified below]"+
											   " min=0 max=65535"+
											   " timepoints_per_partition=0"+
											   " setups_per_partition=0"+
											   " use_deflate_compression"+
											   " export_path="+devilsParam.getOutputDir()+File.separator+"XmlHdf5"+File.separator+file_name_filter+".xml")
   
    reordered_impV.close()
				 
}


/** 
 *  Imports
 */
import loci.formats.ImageReader 
import loci.formats.MetadataTools 
import loci.formats.meta.IMetadata 
import loci.formats.meta.MetadataRetrieve 
import loci.common.services.ServiceFactory 
import loci.formats.services.OMEXMLService

import loci.plugins.in.ImagePlusReader 
import loci.plugins.in.ImporterOptions 
import loci.plugins.in.ImportProcess 
import loci.plugins.util.LociPrefs 

import ij.IJ 
import ij.Prefs 

import ij.ImagePlus 
import ij.ImageStack
import ij.plugin.ChannelSplitter 
import ij.plugin.RGBStackMerge 
import ij.plugin.ImageCalculator 
import ij.process.ImageConverter 

import ij.plugin.FolderOpener
import ij.plugin.HyperStackConverter

import com.google.gson.Gson
import ch.epfl.biop.DEVILS
import ch.epfl.biop.DevilParam