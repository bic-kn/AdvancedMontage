import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.measure.Calibration;
import ij.plugin.ScaleBar;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

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
		
		// Create composite from ChannelOverlays
		ImageStack stack = new ImageStack(inputWidth, inputHeight);
		for (MontageItemOverlay overlay : item.getOverlays()) {
			if (overlay instanceof ChannelOverlay) {
				ImageProcessor channelImageProcessor = extractChannelFromInput(overlay, inputImp);
				stack.addSlice(channelImageProcessor);
			}
		}
		ImagePlus tempImp = new ImagePlus("Composite Temp", stack);
		CompositeImage compositeImage = new CompositeImage(tempImp);
		ImagePlus flattenedImp = compositeImage.flatten();
		
		// TODO Add to output at correct position
		ImageRoi flattenedImpRoi = new ImageRoi(item.getColumn()*inputWidth, item.getRow()*inputHeight, flattenedImp.getProcessor());
		
		Overlay overlay = outputImp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			outputImp.setOverlay(overlay);
		}
		overlay.add(flattenedImpRoi);
		
		// TODO Add scalebar and ROIs
	}
	
	private ImageProcessor extractChannelFromInput(MontageItemOverlay overlay, ImagePlus inputImp) {
		ImageStack stack = inputImp.getStack();
		
		// TODO Return processor for overlay channel
		return stack.getProcessor(1);
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
