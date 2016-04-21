import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

public class MontageCompiler implements Observer {

	private ImagePlus inputImp;
	
	public MontageCompiler(ImagePlus inputImp) {
		this.inputImp = inputImp;
	}

	public ImagePlus compileMontage(MontagePanel panel) {
		Component[] components = panel.getComponents();
		
		int columns = numberOfOutputColumns();
		int rows = numberOfOutputRows();		
		ImagePlus outputImp = new ImagePlus("Montage", new ByteProcessor(columns*inputImp.getWidth(), rows*inputImp.getHeight()));
		
		for (Component component : components) {
			if (component instanceof MontageItem) {
				MontageItem item = (MontageItem) component;
				
				compileItem(item, outputImp);
			}
		}
		
		return outputImp;
	}
	
	void compileItem(MontageItem item, ImagePlus outputImp) {
		int inputWidth = inputImp.getWidth();
		int inputHeight = inputImp.getHeight();
		
		if (item.getOverlays().isEmpty()) {
			return;
		}
		
		LUT[] luts = inputImp.getLuts();
		
		// Create composite from ChannelOverlays
		ImageStack stack = new ImageStack(inputWidth, inputHeight);
		for (MontageItemOverlay overlay : item.getOverlays()) {
			if (overlay instanceof ChannelOverlay) {
				ImageProcessor channelImageProcessor = extractChannelFromInput((ChannelOverlay) overlay);
				channelImageProcessor.setLut(luts[((ChannelOverlay) overlay).getChannel()-1]);
				stack.addSlice(channelImageProcessor);
			}
		}
		ImagePlus tempImp = new ImagePlus("Composite Temp", stack);
		if (IJ.debugMode) {
			tempImp.show();
		}
		CompositeImage compositeImage = new CompositeImage(tempImp);
		compositeImage.setMode(CompositeImage.COMPOSITE);
		if (IJ.debugMode) {
			compositeImage.show();
		}
		ImagePlus flattenedImp = compositeImage.flatten();
		if (IJ.debugMode) {
			flattenedImp.show();
		}
		
		ImageRoi flattenedImpRoi = new ImageRoi(item.getColumn()*inputWidth, item.getRow()*inputHeight, flattenedImp.getProcessor());
		
		Overlay overlay = outputImp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			outputImp.setOverlay(overlay);
		}
		overlay.add(flattenedImpRoi);
		
		// TODO Add scalebar and ROIs
		for (MontageItemOverlay blubb : item.getOverlays()) {
			if (blubb instanceof RoiOverlay) {
				// TODO Do something
//				overlay.add(roi);
			} else if (blubb instanceof ScalebarOverlay) {
				// TODO Do something
//				overlay.add(scalebarRoi);
			}
		}
		
		
		
	}
	
	private ImageProcessor extractChannelFromInput(ChannelOverlay overlay) {
		ImageStack stack = inputImp.getStack();
		
		// TODO Return processor for overlay channel
		return stack.getProcessor(overlay.getChannel());
	}

	int numberOfOutputColumns() {
		return 2;
	}
	
	int numberOfOutputRows() {
		return 2;
	}

	@Override
	public void update(Observable o, Object arg) {
		// FIXME Never actually called
		compileMontage(null);
	}

	/**
	 * Adapted from {@link ScaleBar}.
	 */
//	boolean updateLocation() {
//		Calibration cal = imp.getCalibration();
//		barWidthInPixels = (int)(barWidth/cal.pixelWidth);
//		int width = imp.getWidth();
//		int height = imp.getHeight();
//		int fraction = 20;
//		int x = width - width/fraction - barWidthInPixels;
//		int y = 0;
//		if (location.equals(locations[UPPER_RIGHT]))
//			 y = height/fraction;
//		else if (location.equals(locations[LOWER_RIGHT]))
//			y = height - height/fraction - barHeightInPixels - fontSize;
//		else if (location.equals(locations[UPPER_LEFT])) {
//			x = width/fraction;
//			y = height/fraction;
//		} else if (location.equals(locations[LOWER_LEFT])) {
//			x = width/fraction;
//			y = height - height/fraction - barHeightInPixels - fontSize;
//		} else {
//			if (roiX==-1)
//				 return false;
//			x = roiX;
//			y = roiY;
//		}
//		xloc = x;
//		yloc = y;
//		return true;
//	}
}
