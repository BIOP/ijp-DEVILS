package ch.epfl.biop.lazyprocessing;

import ij.ImagePlus;
import ij.VirtualStack;
import ij.process.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    Function<LocalizedImageProcessor, ImageProcessor> imageProcessorFunction;

    int resultingBitDepth;

    Map<Integer, ImageProcessor> cachedImageProcessor = new ConcurrentHashMap<>();

    public LazyVirtualStack(ImagePlus origin, Function<LocalizedImageProcessor, ImageProcessor> imageProcessorFunction) {
        this.origin = origin;
        this.imageProcessorFunction = imageProcessorFunction;
        // Applies the imageProcessorFunction on the first imageprocessor :
        // allows to know what is the output type of the resulting imagestack
        ImageProcessor ipr = imageProcessorFunction.apply(LocalizedImageProcessor.wrap(origin.getStack().getProcessor(1)));

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
        //this.setColorModel(origin.getStack().getColorModel());
    }

    public synchronized void updateFunction(Function<LocalizedImageProcessor, ImageProcessor> imageProcessorFunction) {
        this.imageProcessorFunction = imageProcessorFunction;
        cachedImageProcessor.clear();
    }

    ImagePlus imagePlusLocalizer = null;

    public void setImagePlusCZTSLocalizer(ImagePlus imp) {
        this.imagePlusLocalizer = imp;
    }

    /** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
    public Object getPixels(int n) {
        ImageProcessor ip = getProcessor(n);
        if (ip!=null)
            return ip.getPixels();
        else
            return null;
    }

    @Override
    public synchronized ImageProcessor getProcessor(int n) {
        //if (!cachedImageProcessor.containsKey(n)) {
        if (imagePlusLocalizer==null) {
            cachedImageProcessor.put(n, imageProcessorFunction.apply(LocalizedImageProcessor.wrap(origin.getStack().getProcessor(n))));
        } else {
            // Localize in czt
            cachedImageProcessor.put(n, imageProcessorFunction.apply(new LocalizedImageProcessor(origin.getStack().getProcessor(n), imagePlusLocalizer.convertIndexToPosition(n))));
        }
        //}
        cachedImageProcessor.get(n).setColorModel(origin.getStack().getProcessor(n).getColorModel());
        return cachedImageProcessor.get(n);
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
