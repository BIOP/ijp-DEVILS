package ch.epfl.biop.preview;


import java.awt.*;  
import java.awt.event.*;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.epfl.biop.lazyprocessing.LazyImagePlusHelper;
import ij.IJ;
import ij.ImagePlus;

import static javax.swing.GroupLayout.Alignment.*; 
	
public class DevilsFrame{
	
	final static Font FONT_L = new Font("SansSerif", Font.PLAIN, 18);
	final static Font FONT_H=new Font ("SansSerif",Font.PLAIN,24);
	final static Border border=new BevelBorder(20);
	
	private final ImagePlus imp=IJ.getImage();
	private final DevilsCalculator dc=new DevilsCalculator(imp);
	private boolean valueMultiCore=false;
	
	private double blur=50;
	private int ball=25;
	private int display=20;
	private JFrame frame= new JFrame("Devils Parameters");
	private Container cPanel=frame.getContentPane();
	private GroupLayout layout=new GroupLayout(cPanel);
	private JButton process;
	private JScrollBar scrollBlur;
	private JSlider slideBall, setDisplay, setSlice;
	private JCheckBox back,multiCore;
	private JLabel label1, label2, label3, label4, label5,label6, valueBall, valueDisplay,valueBlur, slice;
	
	final public ImagePlus preview;
	
	public DevilsFrame(){  
	
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		process=new JButton("Process Slice");
		process.setFont(FONT_L);
		
//		JButton button=new JButton("press");
//		JButton end=new JButton("end");
		
		back=new JCheckBox();
		multiCore=new JCheckBox();
		
		scrollBlur=new JScrollBar();
		scrollBlur.setOrientation(JScrollBar.HORIZONTAL);
		scrollBlur.setSize(500,40);
		scrollBlur.setMinimum(0);
		scrollBlur.setMaximum(500);
		scrollBlur.setValue((int)dc.getBlur());
		
		slideBall=new JSlider();
		slideBall.setOrientation(JSlider.HORIZONTAL);
		slideBall.setSize(500,40);
		slideBall.setMinimum(5);
		slideBall.setMaximum(200);
		slideBall.setValue(dc.getBall());
		
		setDisplay=new JSlider();
		setDisplay.setOrientation(JSlider.HORIZONTAL);
		setDisplay.setSize(500,40);
		setDisplay.setMinimum(1);
		setDisplay.setMaximum(110);
		setDisplay.setValue(20);
				
		setSlice=new JSlider();
		setSlice.setOrientation(JSlider.HORIZONTAL);
		setSlice.setSize(500,40);
		setSlice.setMinimum(0);
		setSlice.setMaximum(100);
		setSlice.setValue(convertSlicePosition(dc.getStackPosition()));
		
		label1=new JLabel("Gaussian Blur: ");
		label1.setFont(FONT_L);
		
		valueBlur=new JLabel(""+ scrollBlur.getValue()/10.0);
		valueBlur.setFont(FONT_L);
		
		label2=new JLabel("Rolling Ball Size: ");
		label2.setFont(FONT_L);
		
		valueBall=new JLabel(""+ slideBall.getValue());
		valueBall.setFont(FONT_L);
		
		label3=new JLabel("Substract Background:");
		label3.setFont(FONT_L);
		
		label4=new JLabel("Display Settings:");
		label4.setFont(FONT_L);
		
		valueDisplay=new JLabel(""+setDisplay.getValue());
		valueDisplay.setFont(FONT_L);

		label5=new JLabel("Slice Position");
		label5.setFont(FONT_L);
		
		label6=new JLabel("Multiprocessor:");
		label6.setFont(FONT_L);
		
		slice=new JLabel(""+setSlice.getValue());
		slice.setFont(FONT_L);
		
		layout.setAutoCreateGaps(true);
	    layout.setAutoCreateContainerGaps(true);
	    cPanel.setLayout(layout);
	    
	    layout.setHorizontalGroup(
	    	layout.createSequentialGroup()
	    		.addGroup(layout.createParallelGroup(LEADING)
	    			.addComponent(scrollBlur)
	    			.addComponent(slideBall)
	    			.addComponent(label3)
	    			.addComponent(label6)
	    			.addComponent(setDisplay)
	    			.addComponent(setSlice)
	    			.addComponent(process))
	    		.addGroup(layout.createParallelGroup(LEADING)
	    			.addComponent(label1)
	    			.addComponent(label2)
	    			.addComponent(back)
	    			.addComponent(multiCore)
	    			.addComponent(label4)
	    			.addComponent(label5))
	    		.addGroup(layout.createParallelGroup(LEADING)
	    			.addComponent(valueBlur)
	    			.addComponent(valueBall)
	    			.addComponent(valueDisplay)
	    			.addComponent(slice))
	    );  
	    layout.setVerticalGroup(
	    		layout.createSequentialGroup()  
	    			.addGroup(layout.createParallelGroup(CENTER)
	    				.addComponent(scrollBlur)  
	    				.addComponent(label1)
	    				.addComponent(valueBlur))
	    			.addGroup(layout.createParallelGroup(CENTER)
	    				.addComponent(slideBall)
	    				.addComponent(label2)
	    				.addComponent(valueBall))
	    			.addGroup(layout.createParallelGroup(CENTER)
	    				.addComponent(label3)
	    				.addComponent(back))
	    			.addGroup(layout.createParallelGroup(CENTER)
		    				.addComponent(label6)
		    				.addComponent(multiCore))
	    			.addGroup(layout.createParallelGroup(CENTER)
	    				.addComponent(setDisplay)
	    				.addComponent(label4)
	    				.addComponent(valueDisplay))
	    			.addGroup(layout.createParallelGroup(CENTER)
	    	    			.addComponent(setSlice)
	    	    			.addComponent(label5)
	    	    			.addComponent(slice))
	    			.addComponent(process)
        );  

	    frame.pack();  
	    frame.setVisible(true);  
	    
	    scrollBlur.addAdjustmentListener(new AdjustmentListener() {
	    	public void adjustmentValueChanged(AdjustmentEvent e) {  
						blur=scrollBlur.getValue()/10.0;
						valueBlur.setText(""+ blur);
						dc.setBlur(blur);
						if (valueMultiCore) dc.multiThreadCalculate();
						else dc.showDisplay();
						LazyImagePlusHelper.redraw(preview);
	                }
	            });
	    slideBall.addChangeListener(new ChangeListener() {
	    	public void stateChanged(ChangeEvent e) {
	    				ball=slideBall.getValue();
	                    valueBall.setText(""+ ball);
	                    dc.setBall(ball);
	                    if (valueMultiCore) dc.multiThreadCalculate();
		    			else dc.showDisplay();
						LazyImagePlusHelper.redraw(preview);
	                }
	            });
	    setDisplay.addChangeListener(new ChangeListener() {
	    	public void stateChanged(ChangeEvent e) {
	    				display=setDisplay.getValue();
	                    valueDisplay.setText(""+ display);
	                    double v=10.0/display;
	                    dc.setDisplay(v);
	                    if (valueMultiCore) dc.multiThreadCalculate();
		    			else dc.showDisplay();
						LazyImagePlusHelper.redraw(preview);
	                }
	            });
	    setSlice.addChangeListener(new ChangeListener() {
	    	public void stateChanged(ChangeEvent e) {
	    				int s=setSlice.getValue();
	                    slice.setText(""+ convertSlicePosition(s));
	                    dc.setSlice(s);
//	                    dc.updateSlice();
	                    if (valueMultiCore) dc.multiThreadCalculate();
		    			else dc.showDisplay();
						LazyImagePlusHelper.redraw(preview);
	                }
	            });
	    process.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e){
					dc.updateSlice();
					
					if (valueMultiCore) dc.multiThreadCalculate();
	    			else dc.showDisplay();
					LazyImagePlusHelper.redraw(preview);
				}
	    });
	    back.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e){
				if (back.isSelected()) {
					IJ.log("true");
					dc.setSubstractBackground(true);
				} else {
					IJ.log("false");
					dc.setSubstractBackground(false);
					
				}
				if (valueMultiCore) dc.multiThreadCalculate();
    			else dc.showDisplay();
				LazyImagePlusHelper.redraw(preview);
			}
	    });
	    multiCore.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e){
				if (multiCore.isSelected()) {
					dc.multiThreadCalculate();
				} else {
					
					dc.showDisplay();
					
				}
				LazyImagePlusHelper.redraw(preview);
			}
	    });

		preview = LazyImagePlusHelper.create(imp, dc::apply, "_DEVILS_PREVIEW");
		preview.show();
	}
	
	int convertSlicePosition(int pos) {
    	int conv=(int)((pos/100.0)*dc.getStackSize());
		return conv;
    }
	private void advancedGroupLayout() {
		 layout.setHorizontalGroup(
			    	layout.createSequentialGroup()
			    		.addGroup(layout.createParallelGroup(LEADING)
			    			.addComponent(scrollBlur)
			    			.addComponent(slideBall)
			    			.addComponent(label3)
			    			.addComponent(setDisplay)
			    			.addComponent(setSlice)
			    			.addComponent(process))
			    		.addGroup(layout.createParallelGroup(LEADING)
			    			.addComponent(label1)
			    			.addComponent(label2)
			    			.addComponent(back)
			    			.addComponent(label4)
			    			.addComponent(label5))
			    		.addGroup(layout.createParallelGroup(LEADING)
			    			.addComponent(valueBlur)
			    			.addComponent(valueBall)
			    			.addComponent(valueDisplay)
			    			.addComponent(slice))
			    );  
			    layout.setVerticalGroup(
			    		layout.createSequentialGroup()  
			    			.addGroup(layout.createParallelGroup(CENTER)
			    				.addComponent(scrollBlur)  
			    				.addComponent(label1)
			    				.addComponent(valueBlur))
			    			.addGroup(layout.createParallelGroup(CENTER)
			    				.addComponent(slideBall)
			    				.addComponent(label2)
			    				.addComponent(valueBall))
			    			.addGroup(layout.createParallelGroup(CENTER)
			    				.addComponent(label3)
			    				.addComponent(back))
			    			.addGroup(layout.createParallelGroup(CENTER)
			    				.addComponent(setDisplay)
			    				.addComponent(label4)
			    				.addComponent(valueDisplay))
			    			.addGroup(layout.createParallelGroup(CENTER)
			    	    			.addComponent(setSlice)
			    	    			.addComponent(label5)
			    	    			.addComponent(slice))
			    			.addComponent(process)
		        );  
	}
}




