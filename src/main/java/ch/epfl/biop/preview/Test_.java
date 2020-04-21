package ch.epfl.biop.preview;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Test_ implements PlugIn{

	@Override
	public void run(String arg) {
		
		DevilsFrame df=new DevilsFrame();

		
		/*		DevilsCalculator dc=new DevilsCalculator(IJ.getImage());
		
		
		ImagePlus imp=dc.multiThreadCalculate();
		
		int w=imp.getWidth();
		int h=imp.getHeight();
		
		ImagePlus out=new ImagePlus();
		ImageProcessor ip = new ByteProcessor(4*w, 2*h);
		int count=1;
		for (int i=0;i<4;i++) {
			for (int j=0;j<2;j++) {
				imp.setSlice(count);
				ImageProcessor trans=imp.getProcessor();
				for (int nx=0;nx<w;nx++) {
					for (int ny=0;ny<h;ny++) {
						ip.putPixel(nx+i*w, ny+j*h, trans.getPixel(nx, ny));
					}
				}
				count++;
			}
				
		}
		
		out.setProcessor(ip);
		out.show();
*/		
		
		
	
	}
}



