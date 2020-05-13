package ch.epfl.biop.preview;

import ch.epfl.biop.DEVILS;
import ch.epfl.biop.DevilMeasure;
import ch.epfl.biop.DevilParam;
import ch.epfl.biop.lazyprocessing.LazyImagePlusHelper;
import ch.epfl.biop.lazyprocessing.LazyVirtualStack;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.function.Function;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Image Processing>DEVILS Preview")
public class DevilsInteractiveCommand extends InteractiveCommand {

    @Parameter
    ImagePlus origin;

    @Parameter(label = "Largest_object_size (in pixel)")
    int objectSize;

    @Parameter(label = "Use Advanced parameters below (you can specify values for each channel, separated by ',')")
    boolean advancedParam;

    @Parameter(label = "Largest_object_size (in pixel)")
    String objectSize_string;

    //@Parameter(label = "Maximum (for normalization step)") TODO : ask Romain if this is used ? doesn't look like it
    //String maxNorm_string;

    @Parameter(label = "Minimum (for final conversion step)")
    String min_final_string;

    @Parameter(label = "Maximum (for final conversion step)")
    String max_final_string;

    @Parameter(label = "Output_bit_depth", choices = {"8-bit", "16-bit", "32-bit"})
    String outputBitDepth_string;

    DevilParam dp;

    DevilMeasure dm;

    ImagePlus liveComputedImage = null;

    int previousBitDepth = -1;

    public void run() {

        dp = new DevilParam(origin, objectSize, advancedParam, min_final_string, max_final_string, objectSize_string, outputBitDepth_string);

        dm = new DevilMeasure(dp);

        Function<ImageProcessor, ImageProcessor> devilsProcessor = (ipr) -> DEVILS.DEVIL_ipr(dp,dm,new int[]{0,0,0,0},ipr);

        if ((liveComputedImage == null)||(previousBitDepth != liveComputedImage.getBitDepth())) {
            // TODO : restore czt location
            if (liveComputedImage != null) {
                liveComputedImage.close();
            }
            // Let's initialize the image
            liveComputedImage = LazyImagePlusHelper.create(origin,devilsProcessor, "_DEVILED");
            liveComputedImage.show();
            previousBitDepth = liveComputedImage.getBitDepth();
        } else {
            ((LazyVirtualStack) liveComputedImage.getStack()).updateFunction(devilsProcessor);
            LazyImagePlusHelper.redraw(liveComputedImage);
        }

    }

}
