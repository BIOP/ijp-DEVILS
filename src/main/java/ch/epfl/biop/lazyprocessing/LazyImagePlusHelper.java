package ch.epfl.biop.lazyprocessing;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.plugin.HyperStackConverter;
import ij.process.ImageProcessor;

import java.util.function.Function;

/**
 * Wraps an ImagePlus plus into a Virtual ImagePlus
 * A function on an ImageProcessor is applied when needed
 *
 */

public class LazyImagePlusHelper extends ImagePlus {

    public static ImagePlus create(ImagePlus origin, String suffix, Function<LocalizedImageProcessor, ImageProcessor> imageProcessorFunction) {
        LazyVirtualStack vds = new LazyVirtualStack(origin, imageProcessorFunction);
        vds.setImagePlusCZTSLocalizer(origin);
        ImagePlus impv = new ImagePlus();
        impv.setStack("", vds);
        ImagePlus out = HyperStackConverter.toHyperStack(impv, origin.getNChannels(), origin.getNSlices(), origin.getNFrames());
        out.setTitle(origin.getTitle()+suffix);
        return out;
    }

    public static ImagePlus create(ImagePlus origin, Function<ImageProcessor, ImageProcessor> imageProcessorFunction, String suffix) {
        Function<LocalizedImageProcessor, ImageProcessor> ipf = (lip) -> imageProcessorFunction.apply(lip.ip);

        LazyVirtualStack vds = new LazyVirtualStack(origin, ipf);
        vds.setImagePlusCZTSLocalizer(origin);
        ImagePlus impv = new ImagePlus();
        impv.setStack("", vds);
        ImagePlus out = HyperStackConverter.toHyperStack(impv, origin.getNChannels(), origin.getNSlices(), origin.getNFrames());
        out.setTitle(origin.getTitle()+suffix);
        return out;
    }

        public static void redraw(ImagePlus imp) {

            // Just try everything...
            imp.updateImage();
            imp.updateAndDraw();
            imp.updateAndRepaintWindow();
            imp.updateChannelAndDraw();
            imp.updateVirtualSlice();

            if (imp instanceof CompositeImage) {
                CompositeImage cimp = (CompositeImage) imp;
                cimp.completeReset();
            }

        }

}
