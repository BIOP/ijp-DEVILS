package ch.epfl.biop.preview;

import ch.epfl.biop.DEVILS;
import ch.epfl.biop.DevilMeasure;
import ch.epfl.biop.DevilParam;
import ch.epfl.biop.lazyprocessing.LazyImagePlusHelper;
import ch.epfl.biop.lazyprocessing.LazyVirtualStack;
import ch.epfl.biop.lazyprocessing.LocalizedImageProcessor;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

import java.util.function.Function;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Image Processing>DEVILS Preview ( Advanced )")
public class DevilsInteractiveAdvancedCommand extends InteractiveCommand {

    @Parameter
    ImagePlus origin;

    //@Parameter(label = "Largest_object_size (in pixel)")
    int objectSize = 0;

    //@Parameter(label = "Use Advanced parameters below (you can specify values for each channel, separated by ',')")
    boolean advancedParam = true;

    @Parameter(required = false, label = "Largest_object_size (in pixel), specify values for each channel, separated by ','")
    String objectSize_string = "";

    //@Parameter(label = "Maximum (for normalization step)") TODO : ask Romain if this is used ? doesn't look like it
    //String maxNorm_string;

    @Parameter(required = false, label = "Minimum (for final conversion step), specify values for each channel, separated by ','")
    String min_final_string = "";

    @Parameter(required = false, label = "Maximum (for final conversion step), specify values for each channel, separated by ','")
    String max_final_string = "";

    @Parameter(required = false, label = "Output_bit_depth", choices = {"8-bit", "16-bit", "32-bit"})
    String outputBitDepth_string = "16-bit";

    @Parameter(label = "Create or Update Preview", callback = "updatePreview", persist = false)
    Button buttonUpdatePreview;

    @Parameter(label = "Start DEVILS with the current parameter", callback = "doProcess", persist = false)
    Button button;

    DevilParam dp;

    DevilMeasure dm;

    ImagePlus liveComputedImage = null;

    public void run() {

    }

    public void updatePreview() {
        dp = new DevilParam(origin, objectSize, advancedParam, min_final_string, max_final_string, objectSize_string, outputBitDepth_string);

        dm = new DevilMeasure(dp);

        // index swapping czt -> zct
        Function<LocalizedImageProcessor, ImageProcessor> devilsProcessor = (ipr) -> DEVILS.DEVIL_ipr(dp,dm,new int[]{ipr.localizationIndex[1]-1,ipr.localizationIndex[0]-1,ipr.localizationIndex[2]-1,0},ipr.ip);

        if ((liveComputedImage!=null)&&(liveComputedImage.isVisible() == false)) liveComputedImage = null; // the user closed the image
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
            liveComputedImage = LazyImagePlusHelper.create(origin,"_DEVILED", devilsProcessor );
            liveComputedImage.show();
            liveComputedImage.setPosition(c, z, t);
        } else {
            if (liveComputedImage.getStack() instanceof LazyVirtualStack) {
                ((LazyVirtualStack) liveComputedImage.getStack()).updateFunction(devilsProcessor);
                LazyImagePlusHelper.redraw(liveComputedImage, origin);
            } else {
                liveComputedImage.hide();
                liveComputedImage.close();
                ImageProcessor ip  = devilsProcessor.apply(new LocalizedImageProcessor(origin.getProcessor(), new int[]{1,1,1}));
                liveComputedImage = new ImagePlus();
                liveComputedImage.setProcessor(ip);
                liveComputedImage.setTitle(origin.getTitle()+"_DEVILED");
                liveComputedImage.show();
            }
        }
    }

    private boolean bitDepthMismatch(int bitDepth, String outputBitDepth_string) {
        if (outputBitDepth_string.equals("8-bit") && bitDepth!=8) return true;
        if (outputBitDepth_string.equals("16-bit") && bitDepth!=16) return true;
        if (outputBitDepth_string.equals("32-bit") && bitDepth!=32) return true;
        return false;
    }

    public void doProcess( ) {
        GenericDialogPlus gd = new GenericDialogPlus("DEVILS parameters");

        String 	defaultPath 		= Prefs.get("ch.epfl.biop.devil.defaultPath"			, Prefs.getImageJDir());
        boolean exportAsXmlHdf5     = Prefs.getBoolean("ch.epfl.biop.devil.exportXmlHdf5"	, false);

        gd.addFileField("Select_File", defaultPath);
        gd.addCheckbox("Export_XmlHdf5", exportAsXmlHdf5);
        gd.showDialog();

        // if "canceled" send error and return
        if ( gd.wasCanceled() ){
            IJ.error("canceled by user");
            return;
        }

        String params = " select_file="+gd.getNextString();
        if (gd.getNextBoolean()) params += " export_xmlhdf5";
        params+= " largest_object_size="+objectSize;

        // TODO : output_directory

        if (advancedParam) params+=" advanced_parameters";
        params+=" object=["+objectSize_string+"]"+
                " minimum=["+min_final_string+"]"+
                " maximum=["+max_final_string+"]"+
                " output_bit_depth="+outputBitDepth_string+
                " output_directory=''";

        final String finalParams = params;

        new Thread(() -> IJ.run("DEVILS",finalParams)).start(); // causes thread lock otherwise ?
    }
}
