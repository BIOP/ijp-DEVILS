package ch.epfl.biop.preview;

import ch.epfl.biop.lazyprocessing.LazyVirtualStack;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class DevilsCalculator extends Thread  {
	private ImageStack stack;
	private ImagePlus input;
	private ImagePlus output;
	private ImageProcessor outIP;
	private int slice, width, height;
	
	private double min=0;
	private double max=255;
	boolean scale=false;
	
	private double blur=50;
	private int ball=25;
	private double displayValue=0.5;
	private boolean background=false;
	private boolean display=false;
	
	
	private final static int n_cpus=Runtime.getRuntime().availableProcessors();
	//=====================Constructors===================================================
	
	public DevilsCalculator(ImagePlus imp){
		if (imp.getStackSize()>1) {
			this.input=imp;
			this.stack=input.getStack();
			this.slice=input.getSlice();
		} else {
			IJ.showMessage("An image stack is needed");
			return;
		}
		this.display=checkDisplay();
		
	}
	 public DevilsCalculator(ImageProcessor ip, double blur, int ball, double displayValue,double min, double max) {
		 this.stack=new ImageStack(ip.getWidth(),ip.getHeight());
		 this.stack.addSlice(ip);
		 this.slice=1;
		 this.blur=blur;
		 this.ball=ball;
		 this.displayValue=displayValue;
		 this.min=min;
		 this.max=max;
		 this.scale=true;
		 
	}
	
	private DevilsCalculator duplicate() {
		DevilsCalculator copy=new DevilsCalculator(this.input);
		copy.slice=input.getSlice();
		copy.blur=this.blur;
		copy.ball=this.ball;
		copy.displayValue= this.displayValue;
		copy.min=this.min;
		copy.max=this.max;
		
		
		return copy;
	}
	 
	//=====================Functions===================================================
	
	

	public void substractBackground(ImagePlus imp){
		BackgroundSubtracter bs=new BackgroundSubtracter();
		bs.rollingBallBackground(imp.getProcessor(), ball,false, false, true, true, true);
		
	}
	//====================setMethods()=====================================================
	
	/* Sets the sigma value for the Gaussian Blur filter								*/ 
	public void setBlur(double rad) {
		this.blur=rad;
	}
	public void setBall(int rad) {
		this.ball=rad;
	}
	public void updateSlice() {
		this.slice=this.input.getSlice();
	}
	public void setDisplay(double v) {
		this.displayValue=v;
	}
	public void showDisplay() {
		if(!this.display) {
			output().show();
			this.display=true;	
		} else {
			output();
		}
		
		
	}
	
	public void showDisplay(ImagePlus imp) {
		imp.show();
		this.display=true;
	}
	
	//====================getMethods()=====================================================
	
		/* Sets the sigma value for the Gaussian Blur filter								*/ 
		public double getBlur() {
			return this.blur;
		}
		public int getBall() {
			return this.ball;
		}
		public int getSlice() {
			return this.slice;
		}
		public double setDisplay() {
			return this.displayValue;
		}
		
		
	
	public void setSubstractBackground(boolean b) {
		this.background=b;
	}
	public int getStackPosition() {
		return input.getCurrentSlice();
	}
	public int getStackSize() {
		return stack.getSize();
	}
	public void setSlice(int s) {
		int stackSize=stack.getSize();
		int pos=(int)((s/100.0)*stackSize);
		input.setSlice(pos);
		
	}
	//=====================ImagePlus===================================================
	private ImagePlus output() {
		 ImagePlus imp;
		
		if (!display) {
//			
			imp= new ImagePlus(); 
			ImageProcessor ip=differenceIP(	stack.getProcessor(slice).duplicate(),
											blurImage(stack.getProcessor(slice).duplicate(),blur)
										);
			 ip=powIP(ip,displayValue);
			 imp.setProcessor(ip);
			 imp.setTitle("Devil Output");
			 if (background) substractBackground(imp);
			 			 
		} else {
			ImageProcessor ip=differenceIP(	stack.getProcessor(slice).duplicate(),
											blurImage(stack.getProcessor(slice).duplicate(),blur)
											);
			ip=powIP(ip,displayValue);
			imp=WindowManager.getImage("Devil Output");
			imp.setProcessor(ip);
			if (background) substractBackground(imp);
		}
		return imp;
	}
	
	ImagePlus fuse(ImagePlus imp) {
		
		int w=imp.getWidth();
		int h=imp.getHeight();
		int rim=20;

		ImagePlus out=new ImagePlus();
		ImageProcessor ip = new ByteProcessor(4*w-3*rim, 2*h);
		int count=1;
		
		int [] transWidth=new int [4];
		int [] start=new int [4];
		int [] shiftX=new int [4];
		
		for (int i=0;i<4;i++) {
			
			if (i==0 || i==3) transWidth[i]=(int)(w-0.5*rim);
			else transWidth[i]=w-rim;
		
			
			if (i==0) {
				start[i]=0;
				shiftX[0]=0;
			}
			else {
				start[i]=start[i-1]+transWidth[i-1];
				shiftX[i]=(int)(rim*0.5);
			}
			
			
//			IJ.log("width:"+transWidth[i]+"     start:"+start[i]+"   siftX"+shiftX[i]);
			
			for (int j=0;j<2;j++) {
				imp.setSlice(count);
				ImageProcessor trans=imp.getProcessor();
				for (int nx=0;nx<transWidth[i];nx++) {
					for (int ny=0;ny<h;ny++) {
						
						ip.putPixel(start[i]+nx, ny+j*h, trans.getPixel(nx+shiftX[i], ny));

//						if (i==0) ip.putPixel(nx+i*w-2*i*rim, ny+j*h, trans.getPixel(nx, ny));
//						else ip.putPixel(nx+i*w-2*i*rim, ny+j*h, trans.getPixel(nx+i*rim, ny));
					}
				}
				count++;
			}
				
		}
		
		out.setProcessor(ip);
		return out;
	}
	//=====================Boolean===================================================
	
	boolean checkDisplay() {
		boolean check=false;
		int list[]=WindowManager.getIDList();
		for (int i=0;i<list.length;i++) {
			if (WindowManager.getImage(list[i]).getTitle()=="Devil Output") {
				check=true;
			}
		}
		
		return check;
	}
	
	
	//=====================Image Processors===================================================
	private ImageProcessor blurImage(ImageProcessor ip, double blur) {
		ImageProcessor out=ip.duplicate();
		out.blurGaussian(blur);
		
		return out;
	}

	private ImageProcessor sqrtIP (ImageProcessor ip) {
		ImageProcessor out=ip.duplicate();
		out=out.convertToFloatProcessor();
		out.sqrt();
		if (!display) {
			out.resetMinAndMax();
			this.min=(int)out.getMin();
			this.max=(int)out.getMax();
			IJ.log("Min: "+min);
			IJ.log("Max: "+max);
		} else 	out.setMinAndMax(min, max);

		out=out.convertToByteProcessor();
		return out;
	}

	private ImageProcessor differenceIP(ImageProcessor source, ImageProcessor diff) {
		
		ImageProcessor out=source.duplicate();
		
		int nx_source=source.getWidth();
		int ny_source=source.getHeight();
		
		int nx_diff=diff.getWidth();
		int ny_diff=diff.getHeight();
		
		if (nx_source==nx_diff && ny_source==ny_diff) {
			for (int i=0;i<nx_source;i++) {
				for (int j=0;j<ny_source;j++) {
					out.putPixelValue(i, j, source.getPixelValue(i, j)-diff.getPixelValue(i, j));
				}
			}
		
		}
		
		return out;
	}
	
	private ImageProcessor powIP(ImageProcessor source,double exp) {
		
		ImageProcessor out=source.duplicate();
		out=out.convertToFloatProcessor();
		
		int nx=source.getWidth();
		int ny=source.getHeight();
		
		for (int i=0;i<nx;i++) {
			for (int j=0;j<ny;j++) {
					out.putPixelValue(i, j, Math.pow(source.getPixelValue(i, j),exp));
			}
		}
		
/*		if (!display) {
			out.resetMinAndMax();
			this.min=(int)out.getMin();
			this.max=(int)out.getMax();
			
			IJ.log("Min: "+min);
			IJ.log("Max: "+max);
			
		} else 	out.setMinAndMax(min, max);
*/		
		IJ.log("ImageProcessor   Min:"+out.getStatistics().min+"   Max"+out.getStatistics().max);
		IJ.log("Setting: Min"+Math.sqrt(this.min)+"   Max"+Math.sqrt(this.max));
		if (this.scale) {
			out.setMinAndMax(Math.sqrt(this.min), Math.sqrt(this.max));
		} else out.resetMinAndMax();
		
		out=out.convertToByteProcessor();
		
		return out;
	}
	
	/** Create a Thread[] array as large as the number of processors available. 
	    * From Stephan Preibisch's Multithreading.java class. See: 
	    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
	    */  
    private Thread[] newThreadArray() {  
        int n_cpus = Runtime.getRuntime().availableProcessors();  
        return new Thread[n_cpus];  
    }  
	  
	/** Start all given threads and wait on each of them until all are done. 
	* From Stephan Preibisch's Multithreading.java class. See: 
	* http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
	*/  
    public static void startAndJoin(DevilsCalculator[] calculate)  
    {  
        for (int ithread = 0; ithread < n_cpus; ++ithread)  
        {  
            calculate[ithread].setPriority(Thread.NORM_PRIORITY);  
            calculate[ithread].start();  
        }  
  
        try  
        {     
            for (int ithread = 0; ithread < n_cpus; ++ithread)  
                calculate[ithread].join();  
        } catch (InterruptedException ie)  
        {  
            throw new RuntimeException(ie);  
        }  
    }
    public static ImageStack getResultStack(DevilsCalculator [] diffArray){
    	
    	int w=diffArray[0].width;
    	int h=diffArray[0].height;
    	
    	ImageStack stack=new ImageStack(w,h);
    	
    	for (int i=0;i<diffArray.length;i++){
    		ImageProcessor ip=diffArray[i].getResultProcessor();
    		stack.addSlice(ip);
    	}
    	
    	return stack;
    }
    /* ******************************************************************************
     * Defines the Array of DevilCalculators()
     * @return array of DevilsCalculators
     ********************************************************************************/
    
    public DevilsCalculator []  getArray() {								
		
    	DevilsCalculator array []=new DevilsCalculator[n_cpus];
		
		int width=this.input.getWidth();
		int height=this.input.getHeight();
		
		double max=this.input.getProcessor().getStatistics().max;
		double min=this.input.getProcessor().getStatistics().min;
		
		int rim=20;
		
		int col=n_cpus/2;
		int row=2;
		
//		int subWidth=(int)Math.round(0.5+((width+2*(col-1)*rim)/(double)col));
		
		int subWidth=(width+(col-1)*rim)/col;
//		IJ.log("Subwidth:"+subWidth);
		
		int subHeight=height/row;
		int count=0;
		
		for (int i=0;i<col;i++){
			
			int x=i*subWidth-i*rim;
			
//			IJ.log("x="+x);
			for (int j=0;j<row;j++) {
				
				int y=j*subHeight;
				Roi roi=new Roi(x,y,subWidth,subHeight);
				input.setRoi(roi);
				array[count] =new DevilsCalculator(input.crop().getProcessor(),this.blur,this.ball,this.displayValue,min,max);
				count++;
			}	
		}
			
//		
		return array;
    }
    /**********************************************************************************************************************************
     * method to do the calculation with multi-threads 
     * calls the method which defines the array of Devils Calculators
     * @return ImagePlus
     **********************************************************************************************************************************/
    
    ImagePlus multiThreadCalculate(){
		
//		long start=System.currentTimeMillis();
		
		final DevilsCalculator [] calculate = this.duplicate().getArray(); 
//		
		
		startAndJoin(calculate);
		
//		long end=System.currentTimeMillis();    
//		IJ.log("Processing time convolution in msec: "+(end-start));
//		   
		ImagePlus imp;
		
		if (!display) {
			imp=new ImagePlus("Conv",DevilsCalculator.getResultStack(calculate));
		} else {
			imp=WindowManager.getImage("Devil Output");
			imp.setProcessor(this.fuse(new ImagePlus("",DevilsCalculator.getResultStack(calculate))).getProcessor());
		}
	    
	                	
		return imp;
	    
	}    

    public ImageProcessor getResultProcessor(){
		return this.outIP;
	}
    public void run() {
    	this.outIP=this.output().getProcessor();

    }


    // --------------------------------------------------

	/**
	 * Function which applies the current set of operation on an imageprocessor
	 * this function can be directly fed into a {@link LazyImagePlus}
	 * @param in
	 * @return
	 */
	public ImageProcessor apply(ImageProcessor in) {
		ImageProcessor ip=differenceIP(	in.duplicate(),
				blurImage(in.duplicate(),blur)
		);
		ip=powIP(ip,displayValue);

		if (background) {
			(new BackgroundSubtracter()).rollingBallBackground(ip, ball,false, false, true, true, true);
		}

		return ip;
	}
}
