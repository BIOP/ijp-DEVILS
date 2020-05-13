package ch.epfl.biop.lazyprocessing;

import ij.ImagePlus;
import ij.VirtualStack;
import ij.process.*;

import java.util.function.Function;

/**
 * Class which copies an ImagePlus, except that it applies an operation to modify
 * each ImageProcessor when it is requested.
 *
 */

public class LazyVirtualStack extends VirtualStack {

    // Original ImagePlus
    ImagePlus origin;

    // Operation applied on an ImageProcessor
    Function<ImageProcessor, ImageProcessor> imageProcessorFunction;

    int resultingBitDepth;

    public LazyVirtualStack(ImagePlus origin, Function<ImageProcessor, ImageProcessor> imageProcessorFunction) {
        this.origin = origin;
        this.imageProcessorFunction = imageProcessorFunction;
        // Applies the imageProcessorFunction on the first imageprocessor :
        // allows to know what is the output type of the resulting imagestack
        ImageProcessor ipr = imageProcessorFunction.apply(origin.getStack().getProcessor(1));

        if (ipr instanceof ByteProcessor) {
            resultingBitDepth = 8;
        } else if (ipr instanceof ShortProcessor) {
            resultingBitDepth = 16;
        } else if (ipr instanceof FloatProcessor) {
            resultingBitDepth = 32;
        } else if (ipr instanceof ColorProcessor) {
            resultingBitDepth = 24;
        } else {
            throw new UnsupportedOperationException("Unknown resulting ImageProcessor");
        }

    }


    public void updateFunction(Function<ImageProcessor, ImageProcessor> imageProcessorFunction) {
        this.imageProcessorFunction = imageProcessorFunction;
    }

    /** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
    public Object getPixels(int n) {
        ImageProcessor ip = imageProcessorFunction.apply(origin.getStack().getProcessor(n));
        if (ip!=null)
            return ip.getPixels();
        else
            return null;
    }

    @Override
    public ImageProcessor getProcessor(int n) {
        return imageProcessorFunction.apply(origin.getStack().getProcessor(n));
    }

    @Override
    public int getBitDepth() {
        return resultingBitDepth;
    }

    @Override
    public int getSize() {
        return origin.getStack().getSize();
    }

    @Override
    public int getHeight() {
        return origin.getHeight();
    }

    @Override
    public int getWidth() {
        return origin.getWidth();
    }

}
