package ch.epfl.biop.preview;

import ch.epfl.biop.DEVILS;
import ch.epfl.biop.DevilParam;
import com.google.gson.Gson;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import ij.plugin.HyperStackConverter;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Image Processing>Open DEVILS Folder")
public class DevilsOpener implements Command {

    @Parameter(label = "Select Devils json file")
    File jsonFile;

    @Override
    public void run() {
        try {
            DevilParam dp = (new Gson()).fromJson(new FileReader(jsonFile.getAbsolutePath()), DevilParam.class);
            for (int it = 0;it<dp.getnSeries();it++) {
                // Open the serie as a virtual stack

                // and define the name accordingly using
                String file_name_filter = dp.imageName;

                if (dp.getnSeries() > 1) file_name_filter += "_s" + it + "-"; // TODO : check potential issue with series > 9

                System.out.println("nSeries = "+dp.getnSeries());
                System.out.println(file_name_filter);

                ImagePlus impV = FolderOpener.open(dp.getOutputDir(), "virtual file=[" + file_name_filter + "]");

                System.out.println(impV.getNSlices());

                System.out.println(dp.nChannel);
                System.out.println(dp.nSlice);
                System.out.println(dp.nFrame);

                // re-order using ch_count and total plane number
                ImagePlus reordered_impV = HyperStackConverter.toHyperStack(impV, dp.nChannel, dp.nSlice, dp.nFrame, "Composite");

                // set calibration
                reordered_impV.getCalibration().pixelWidth = dp.voxelSize[0];
                reordered_impV.getCalibration().pixelHeight = dp.voxelSize[1];
                reordered_impV.getCalibration().pixelDepth = dp.voxelSize[2];

                reordered_impV.setTitle(file_name_filter);

                reordered_impV.show();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
