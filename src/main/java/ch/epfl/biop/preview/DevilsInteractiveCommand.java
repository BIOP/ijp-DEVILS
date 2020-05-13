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
    int objectSize = 25;

    //@Parameter(label = "Use Advanced parameters below (you can specify values for each channel, separated by ',')")
    boolean advancedParam = false;

    //@Parameter(required = false, label = "Largest_object_size (in pixel)")
    String objectSize_string = "";

    //@Parameter(label = "Maximum (for normalization step)") TODO : ask Romain if this is used ? doesn't look like it
    //String maxNorm_string;

    //@Parameter(required = false, label = "Minimum (for final conversion step)")
    String min_final_string = "";

    //@Parameter(required = false, label = "Maximum (for final conversion step)")
    String max_final_string = "";

    //@Parameter(required = false, label = "Output_bit_depth", choices = {"8-bit", "16-bit", "32-bit"})
    String outputBitDepth_string;

    DevilParam dp;

    DevilMeasure dm;

    ImagePlus liveComputedImage = null;

    public void run() {

        dp = new DevilParam(origin, objectSize, advancedParam, min_final_string, max_final_string, objectSize_string, outputBitDepth_string);

        dm = new DevilMeasure(dp);

        Function<ImageProcessor, ImageProcessor> devilsProcessor = (ipr) -> DEVILS.DEVIL_ipr(dp,dm,new int[]{0,0,0,0},ipr);

        if ((liveComputedImage == null)||(bitDepthMismatch(liveComputedImage.getBitDepth(), outputBitDepth_string))) {
            int c = 1;
            int z = 1;
            int t = 1;
            if (liveComputedImage != null) {
                liveComputedImage.close();
                c = liveComputedImage.getC();
                t = liveComputedImage.getT();
                z = liveComputedImage.getZ();
            }
            // Let's initialize the image
            liveComputedImage = LazyImagePlusHelper.create(origin,devilsProcessor, "_DEVILED");
            liveComputedImage.show();
            liveComputedImage.setPosition(c, z, t);
        } else {
            ((LazyVirtualStack) liveComputedImage.getStack()).updateFunction(devilsProcessor);
            LazyImagePlusHelper.redraw(liveComputedImage);
        }

    }

    private boolean bitDepthMismatch(int bitDepth, String outputBitDepth_string) {
        if (outputBitDepth_string.equals("8-bit") && bitDepth!=8) return true;
        if (outputBitDepth_string.equals("16-bit") && bitDepth!=16) return true;
        if (outputBitDepth_string.equals("32-bit") && bitDepth!=32) return true;
        return false;
    }

}
