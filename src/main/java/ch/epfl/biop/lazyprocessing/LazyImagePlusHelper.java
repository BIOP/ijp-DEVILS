package ch.epfl.biop.lazyprocessing;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackConverter;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.*;
import java.lang.reflect.Field;
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
        ImagePlus imp = new ImagePlus();
        imp.setStack("", vds);

        ImagePlus out = null;
        if (origin.getNChannels()*origin.getNSlices()*origin.getNFrames() == 1) {
            out = imp;
            ImageStack is;
        } else {
            out = HyperStackConverter.toHyperStack(imp, origin.getNChannels(), origin.getNSlices(), origin.getNFrames());
        }

        out.setDisplayMode(CompositeImage.GRAYSCALE);
        out.setTitle(origin.getTitle()+suffix);
        if ((origin.isComposite())&&(out.isComposite())) {
            ((CompositeImage)out).setLuts(origin.getLuts().clone());
        }
        out.setPosition(origin.getC(), origin.getZ(), origin.getT());
        return out;
    }

    public static void redraw(ImagePlus imp, ImagePlus impOrig) {

        imp.updateImage();
        imp.updateAndDraw();
        imp.updateAndRepaintWindow();
        imp.updateChannelAndDraw();
        imp.updateVirtualSlice();

        if (imp instanceof CompositeImage) {
            CompositeImage cimp = (CompositeImage) imp;

            double[] min = new double[cimp.getNChannels()];
            double[] max = new double[cimp.getNChannels()];

            for (int i=0;i<cimp.getNChannels();i++) {
                min[i] = cimp.getChannelLut(i+1).min;
                max[i] = cimp.getChannelLut(i+1).max;
            }

            cimp.setChannelsUpdated();

            LUT[] copied = impOrig.getLuts().clone();

            for (int i=0;i<cimp.getNChannels();i++) {
                copied[i].min = min[i];
                copied[i].max = max[i];
            }
            ((CompositeImage)imp).setLuts(copied);

        }

    }

}
