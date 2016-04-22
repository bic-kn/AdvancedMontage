import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.Observable;
import java.util.Observer;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.ScaleBar;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

public class MontageCompiler implements Observer {

	private ImagePlus inputImp;
	private int maxColumns = -1;
	private int maxRows = -1;
	private boolean boldText;
	private boolean serifFont;
	private boolean hideText = false;
	static final String[] locations = {"Upper Right", "Lower Right", "Lower Left", "Upper Left", "At Selection"};
	static final int UPPER_RIGHT=0, LOWER_RIGHT=1, LOWER_LEFT=2, UPPER_LEFT=3, AT_SELECTION=4;
	
	public MontageCompiler(ImagePlus inputImp) {
		this.inputImp = inputImp;
	}

	public ImagePlus compileMontage(MontagePanel panel) {
		Component[] components = panel.getComponents();
		
		int columns = numberOfOutputColumns(panel);
		int rows = numberOfOutputRows(panel);
		
		// TODO Integrate padding
		int outputWidth = computeOutputWidth(columns, 0); //, paddingWidth);
		int outputHeight = computeOutputHeight(rows, 0); //, paddingWidth);
		
		ImagePlus outputImp = new ImagePlus("Montage", new ByteProcessor(outputWidth, outputHeight));
		
		for (Component component : components) {
			if (component instanceof MontageItem) {
				MontageItem item = (MontageItem) component;
				
				compileItem(item, outputImp);
			}
		}
		
		return outputImp;
	}

	private int computeOutputWidth(int columns, int paddingWidth) {
		return columns*inputImp.getWidth() + (columns-1)*paddingWidth;
	}
	
	private int computeOutputHeight(int rows, int paddingWidth) {
		return rows*inputImp.getHeight() + (rows-1)*paddingWidth;
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
		
		for (MontageItemOverlay itemOverlay : item.getOverlays()) {
			if (itemOverlay instanceof RoiOverlay) {
				addROIsToOverlay(overlay);
			} else if (itemOverlay instanceof ScalebarOverlay) {
				addScalebarToOverlay(item, overlay);
			}
		}
	}

	private void addROIsToOverlay(Overlay overlay) {
		RoiManager roiManager = RoiManager.getInstance();
		if (roiManager == null) {
			return;
		}

		Roi[] roisInManager = roiManager.getRoisAsArray();
		for (Roi roi : roisInManager) {
			overlay.add(roi);
		}
	}

	private ImageProcessor extractChannelFromInput(ChannelOverlay overlay) {
		ImageStack stack = inputImp.getStack();
		return stack.getProcessor(overlay.getChannel());
	}

	int numberOfOutputColumns(MontagePanel panel) {
//		if (maxColumns > 0) {
//			return maxColumns;
//		}
		
		computeAndSetMaxColumnsAndRows(panel);
		return maxColumns;
	}
	
	int numberOfOutputRows(MontagePanel panel) {
//		if (maxRows > 0) {
//			return maxRows;
//		}
		
		computeAndSetMaxColumnsAndRows(panel);
		return maxRows;
	}

	private void computeAndSetMaxColumnsAndRows(MontagePanel panel) {
		maxColumns = -1;
		maxRows = -1;
		
		Component[] components = panel.getComponents();		
		for (Component component : components) {
			if (component instanceof MontageItem) {
				MontageItem item = (MontageItem) component;
				if (!item.isEmpty()) {
					int itemColumn = item.getColumn()+1;
					int itemRow = item.getRow()+1;
					
					maxColumns = itemColumn > maxColumns ? itemColumn : maxColumns;
					maxRows = itemRow > maxRows ? itemRow : maxRows;
				}
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// FIXME Never actually called
		compileMontage(null);
	}
	
	/**
	 * Adapted from {@link ScaleBar}.
	 */
	private Point computeScalebarLocation(MontageItem item) {
		// TODO Get from global settings
		double barWidth = 4.5d;
		
		// TODO Get from global settings
		String location = "Lower Right";
		
		// TODO Get from global settings
		int barHeightInPixels = 2;
		
		// TODO Get from global settings
		int fontSize = 42;
		
		// TODO Get from global settings
		int paddingWidth = 10;
		
		Calibration cal = inputImp.getCalibration();
		int barWidthInPixels = (int)(barWidth/cal.pixelWidth);
		
		int width = inputImp.getWidth();
		int height = inputImp.getHeight();
		int fraction = 20;
		int x = width - width/fraction - barWidthInPixels;
		int y = 0;
		
		if (location.equals(locations[UPPER_RIGHT]))
			 y = height/fraction;
		else if (location.equals(locations[LOWER_RIGHT]))
			y = height - height/fraction - barHeightInPixels - fontSize;
		else if (location.equals(locations[UPPER_LEFT])) {
			x = width/fraction;
			y = height/fraction;
		} else if (location.equals(locations[LOWER_LEFT])) {
			x = width/fraction;
			y = height - height/fraction - barHeightInPixels - fontSize;
		}
		
		int xShift = item.getColumn()*inputImp.getWidth() + item.getColumn()*paddingWidth;
		int yShift = item.getRow()*inputImp.getHeight() + item.getRow()*paddingWidth;
		
		return new Point(xShift + x, yShift + y);
	}
	
	/**
	 * Adapted from {@link ScaleBar}.
	 */
	private void addScalebarToOverlay(MontageItem item, Overlay overlay) {
		// TODO Remove exisiting scalebar from overlay?
		// overlay.remove(SCALE_BAR);
		
		// TODO Get from global settings
		double barWidth = 4.5d;
		
		// TODO Get from global settings
		String location = "Lower Right";
		
		// TODO Get from global settings
		int barHeightInPixels = 2;
		
		// TODO Get from global settings
		int fontSize = 42;
		
		// TODO Get from global settings
		int paddingWidth = 10;
		
		// TODO Get from global settings
		Color color = Color.WHITE;

		Point scalebarLocation = computeScalebarLocation(item);
		
		int x = scalebarLocation.x;
		int y = scalebarLocation.y;
		int fontType = boldText?Font.BOLD:Font.PLAIN;
		String face = serifFont?"Serif":"SanSerif";
		Font font = new Font(face, fontType, fontSize);
		String label = getLength(barWidth) + " "+ getUnits(inputImp);
		ImageProcessor ip = inputImp.getProcessor();
		ip.setFont(font);
		int textWidth = hideText?0:ip.getStringWidth(label);
		Calibration cal = inputImp.getCalibration();
		int barWidthInPixels = (int)(barWidth/cal.pixelWidth);
		Roi bar = new Roi(x, y, barWidthInPixels, barHeightInPixels);
		bar.setFillColor(color);
		bar.setStrokeColor(color);
		overlay.add(bar);
		
		int xoffset = (barWidthInPixels - textWidth)/2;
		int yoffset =  barHeightInPixels + (hideText?0:fontSize+fontSize/4);
		if (!hideText) {
			TextRoi text = new TextRoi(x+xoffset, y+yoffset, label, font);
			text.setStrokeColor(color);
			overlay.add(text);
		}
	}
	
	String getUnits(ImagePlus imp) {
		String units = imp.getCalibration().getUnits();
		if (units.equals("microns"))
			units = IJ.micronSymbol+"m";
		return units;
	}
	
	String getLength(double barWidth) {
		int digits = (int)barWidth==barWidth?0:1;
		if (barWidth<1.0) digits=1;
		if (digits==1) {
			String s = IJ.d2s(barWidth/0.1, 2);
			if (!s.endsWith(".00")) digits = 2;
		}
		return IJ.d2s(barWidth, digits);
	}
}
