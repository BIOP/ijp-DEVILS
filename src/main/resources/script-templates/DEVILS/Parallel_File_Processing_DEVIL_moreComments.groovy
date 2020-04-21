//@File(label="Main Directory", style="directory") mainDir
// @String(label="Max size of objects (pixel)", value="25") size_String
// @String(label="Output image bit depth?",choices={"16","32"}) output_bitDepth
// @String(value="Parameters for intensity rescaling if the output bit depth is set to 16", visibility="MESSAGE") param16bit
// @String(label="Min intensity", value="-100") min_String
// @String(label="Max intensity", value="10000") max_String

import groovyx.gpars.GParsExecutorsPool
import groovyx.gpars.GParsPool
import groovy.io.FileType
import ij.*
import IJ.*
import File.*

def allFilesList = []

debugMode=false

// get the "parent" folder
def parentDir = mainDir.getAbsolutePath()
def parentDir_str = parentDir+File.separator
// make an Output Folder
def outputDir_str =  parentDir_str+"DEVILed"+File.separator
def outputDir = new File(outputDir_str)
outputDir.mkdir();

// here some print out in debugMode
if (debugMode)print("\nINput Dir : "+parentDir_str)
if (debugMode)print("\nOUTput Dir : "+outputDir_str)


// Retrieve all the "tif" files in all the subfolders
mainDir.eachFileRecurse (FileType.FILES) { file ->
		// except if the path contains 'DEVIL' 
		// to avoid applying DEVIL twice
		if( !file.getAbsolutePath().contains("DEVIL") ){
			if(  file.getName().endsWith(".tif")  ) {
				allFilesList << file
			}
		}
}
if (debugMode) print("\n "+allFilesList)

// make a new lsit by replacing the path directory by the output directory
def allOutputList = allFilesList.collect{ new File (it.getAbsolutePath().replace(parentDir_str, outputDir_str)) }
if (debugMode) print("\n "+allOutputList)

// from the allOutputList
// create a folderList , ends with '\\', File.separator
def folderList = allOutputList.collect{ new File (it.getAbsolutePath().substring(0, it.getAbsolutePath().lastIndexOf( File.separator ) ) ) }
// from this folderList keep only one occurence of it
def folderListUnik = new HashSet( folderList )
if (debugMode) print("\n"+folderListUnik)
//and create the folders
folderListUnik.each{  
	new File("$it").mkdir()
	if (debugMode) print("\nCreates "+"$it")
}


// here we define the number of threads 
// the higher the faster
// but try to keep it low (below cores number)
// or the processing could stop (without crash or exception sent, just stop). we don't know why yet !
def nThreads = 32

//Gpars
GParsExecutorsPool.withPool(nThreads) {
	allFilesList.eachWithIndexParallel{ thefile, idx ->
		def filePath_str 		= thefile.getAbsolutePath()
		def outputFolder_str 	= allOutputList[idx].getParent() + File.separator
		
		if ( allOutputList[idx].exists() ){
			print("\nOutput file already exist : "+filePath_str )
		}else{		
			if (debugMode)print("\nProcessing file "+idx+" : "+filePath_str )
			if (debugMode)print("\nOutput folder "+idx+" : "+outputFolder_str )
				
			// here is the DEVIL runner ! 
			IJ.run("DEVILS", "browse="+filePath_str+
							" select_file="+filePath_str+
							" largest_object_size=25"+
							" advanced_parameters"+
							" output_directory="+outputFolder_str+
							" minimum=["+min_String+"]"+
			                " maximum=["+max_String+"]"+
							" object=["+size_String+"]"+
							" output_bit_depth=["+output_bitDepth+"-bit]");	
			//clear IJ environment
			//IJ.run("Collect Garbage"); 
		}						
	}
}

print("\nDONE ");
IJ.beep();
