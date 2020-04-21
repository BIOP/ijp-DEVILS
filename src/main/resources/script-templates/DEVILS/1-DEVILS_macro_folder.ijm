call("BIOP_LibInstaller.installLibrary", "BIOP"+File.separator+"BIOPLib.ijm");

dir = getDirectory("");
fileList = getFileList(dir);

for( i = 0 ; i < lengthOf(fileList) ; i++){
	if (isImage(fileList[i])){
		run("DEVILS", 	"select_file=["+dir+fileList[i]+"] "+
						"largest_object_size=25 "+
						"advanced_parameters "+
						"output_directory=["+dir+"/DEVILS/] "+
						"minimum=-500 maximum=5000 object=25 output_bit_depth=16-bit");
	}
}

