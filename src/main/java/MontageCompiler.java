import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

public class MontageCompiler implements ActionListener {

	private int maxColumns = -1;
	private int maxRows = -1;
	private boolean boldText;
	private boolean serifFont;
	private boolean hideText = false;
	static final String[] locations = {"Upper Right", "Lower Right", "Lower Left", "Upper Left", "At Selection"};
	static final int UPPER_RIGHT=0, LOWER_RIGHT=1, LOWER_LEFT=2, UPPER_LEFT=3, AT_SELECTION=4;
	private MontageTool tool;
	
	public MontageCompiler(MontageTool tool) {
		this.tool = tool;
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @return {@link ImagePlus} with the images and the scalebar (if activated)
	 *         as overlay
	 */
	public ImagePlus compileMontage() {	
		Collection<MontageItem> montageItems = tool.getMontageItems();
		
		int columns = numberOfOutputColumns(montageItems);
		int rows = numberOfOutputRows(montageItems);
		
		int outputWidth = computeOutputWidth(columns, tool.getPaddingWidth());
		int outputHeight = computeOutputHeight(rows, tool.getPaddingWidth());
		
		ImageProcessor outputIp = new ColorProcessor(outputWidth, outputHeight);
		outputIp.setColor(tool.getPaddingColor());
		outputIp.fill();
		ImagePlus outputImp = new ImagePlus("Montage", outputIp);
		
		for (MontageItem montageItem : montageItems) {
			compileItem(montageItem, outputImp);
			outputImp = outputImp.flatten();
			cleanMontageTitle(outputImp);
		}
		
		return outputImp;
	}

	/**
	 * TODO Documentation
	 * 
	 * @param outputImp
	 */
	private void cleanMontageTitle(ImagePlus outputImp) {
		String outputTitle = outputImp.getTitle();
		String[] splitOutputTitle = outputTitle.split("Flat_");
		outputImp.setTitle(splitOutputTitle[1]);
	}

	/**
	 * TODO Documentation
	 * 
	 * @param columns
	 * @param paddingWidth
	 * @return
	 */
	private int computeOutputWidth(int columns, int paddingWidth) {
		return columns*tool.getImp().getWidth() + (columns-1)*paddingWidth;
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @param rows
	 * @param paddingWidth
	 * @return
	 */
	private int computeOutputHeight(int rows, int paddingWidth) {
		return rows*tool.getImp().getHeight() + (rows-1)*paddingWidth;
	}

	/**
	 * Compiles an image from a {@link MontageItem} and adds it to the overlay
	 * of outputImp.
	 * 
	 * @param item
	 * @param outputImp
	 */
	private void compileItem(MontageItem item, ImagePlus outputImp) {
		int inputWidth = tool.getImp().getWidth();
		int inputHeight = tool.getImp().getHeight();
		
		if (!item.hasDrawnOverlay()) {
			return;
		}
		
		LUT[] availableLuts = tool.getAvailableLuts();
		List<LUT> usedLuts = new ArrayList<>();
		
		// Create composite from ChannelOverlays
		ImageStack stack = new ImageStack(inputWidth, inputHeight);
		for (MontageItemOverlay overlay : item.getOverlays()) {
			if (overlay instanceof ChannelOverlay && overlay.isDrawn()) {
				ImageProcessor channelImageProcessor = extractChannelFromInput((ChannelOverlay) overlay);
				channelImageProcessor.setLut(availableLuts[((ChannelOverlay) overlay).getChannel()-1]);
				stack.addSlice(channelImageProcessor);
				usedLuts.add(availableLuts[((ChannelOverlay) overlay).getChannel()-1]);
			}
		}
		ImagePlus tempImp = new ImagePlus("Composite Temp", stack);
		if (IJ.debugMode) {
			tempImp.show();
		}
		CompositeImage compositeImage = new CompositeImage(tempImp, CompositeImage.COMPOSITE);
		compositeImage.setLuts((LUT[]) usedLuts.toArray(new LUT[usedLuts.size()]));
		if (IJ.debugMode) {
			compositeImage.show();
		}
		ImagePlus flattenedImp = compositeImage.flatten();
		if (IJ.debugMode) {
			flattenedImp.show();
		}
		
		Overlay flattenedImpOverlay = flattenedImp.getOverlay();
		if (flattenedImpOverlay == null) {
			flattenedImpOverlay = new Overlay();
			flattenedImp.setOverlay(flattenedImpOverlay);
		}
		
		Overlay outputImpOverlay = outputImp.getOverlay();
		if (outputImpOverlay == null) {
			outputImpOverlay = new Overlay();
			outputImp.setOverlay(outputImpOverlay);
		}
		
		Overlay scalebarOverlay = new Overlay();
		for (MontageItemOverlay itemOverlay : item.getOverlays()) {
			if (itemOverlay instanceof RoiOverlay && itemOverlay.isDrawn()) {
				addROIsToOverlay(item, flattenedImpOverlay);
				flattenedImp = flattenedImp.flatten();
			} else if (itemOverlay instanceof ScalebarOverlay && itemOverlay.isDrawn()) {
				addScalebarToOverlay(item, scalebarOverlay);
			}
		}
		
		ImageRoi flattenedImpRoi = new ImageRoi(
				item.getColumn() * inputWidth + item.getColumn() * tool.getPaddingWidth(),
				item.getRow() * inputHeight + item.getRow() * tool.getPaddingWidth(), flattenedImp.getProcessor());
		
		// NB: The sequence of adding to the overlay determines the arrangement
		outputImpOverlay.add(flattenedImpRoi);
		for (Roi r : scalebarOverlay.toArray()) {
			outputImpOverlay.add(r);
		}
	}

	/**
	 * Adds one of the following sets of {@link Roi}s to the provided
	 * {@code Overlay} in the provided order (precedence from first to last):
	 * 
	 * <ol>
	 * <li>The active ROI of the image to each of the selected channels</li>
	 * <li>The selected ROIs in the RoiManager when the ROI option was activated
	 * </li>
	 * <li>The ROIs from the RoiManager if they belong to the drawn channel</li>
	 * </ol>
	 * 
	 * @param item
	 *            {@link MontageItem} from which the required information is
	 *            extracted, i.e. the active ROIs from the RoiManager
	 * @param overlay
	 *            The {@link Overlay} to which the {@link Roi}s are added
	 */
	private void addROIsToOverlay(MontageItem item, Overlay overlay) {
		// TODO Refactor code
		/* The image has an active ROI */
		Roi activeRoi = tool.getImp().getRoi();
		if (activeRoi != null) {
			overlay.add(activeRoi);
			
			// TODO Clear the ROIs from the RoiOverlay
			return;
		}
		
		for (MontageItemOverlay itemOverlay : item.getOverlays()) {
			if (itemOverlay.isDrawn()) {
				if (itemOverlay instanceof RoiOverlay) {
					Roi[] rois = ((RoiOverlay) itemOverlay).getRois();
					if (rois == null) {
						/* The ROIs for each slice are drawn */
						RoiManager roiManager = RoiManager.getRoiManager();
						Roi[] roisInManager = roiManager.getRoisAsArray();
						for (Roi roi : roisInManager) {
							for (MontageItemOverlay itemOverlay2 : item.getOverlays()) {
								if (itemOverlay2.isDrawn()) {
									if (itemOverlay2 instanceof ChannelOverlay) {
										int itemOverlayChannel = ((ChannelOverlay) itemOverlay2).getChannel();
										if (roi.getPosition() == itemOverlayChannel) {
											overlay.add(roi);
										}
									}
								}
							}
						}
						
						return;
					}
					
					/* The selected ROIs are drawn */
					for (Roi roi : rois) {
						overlay.add(roi);
					}
				}
			}
		}
	}

	/**
	 * TODO Documentation
	 * 
	 * @param overlay
	 * @return
	 */
	private ImageProcessor extractChannelFromInput(ChannelOverlay overlay) {
		ImageStack stack = tool.getImp().getStack();
		return stack.getProcessor(overlay.getChannel());
	}

	int numberOfOutputColumns(Collection<MontageItem> montageItems) {
		// TODO Implement check for change in panel and return values if no
		// change was observed
//		if (maxColumns > 0) {
//			return maxColumns;
//		}
		
		computeAndSetMaxColumnsAndRows(montageItems);
		return maxColumns;
	}
	
	int numberOfOutputRows(Collection<MontageItem> montageItems) {
		// TODO Implement check for change in panel and return values if no
		// change was observed
//		if (maxRows > 0) {
//			return maxRows;
//		}
		
		computeAndSetMaxColumnsAndRows(montageItems);
		return maxRows;
	}

	private void computeAndSetMaxColumnsAndRows(Collection<MontageItem> montageItems) {
		// Reset before each iteration
		maxColumns = -1;
		maxRows = -1;
		
		for (MontageItem item : montageItems) {
			if (!item.isEmpty()) {
				int itemColumn = item.getColumn()+1;
				int itemRow = item.getRow()+1;
				
				maxColumns = itemColumn > maxColumns ? itemColumn : maxColumns;
				maxRows = itemRow > maxRows ? itemRow : maxRows;
			}
		}
	}

	/**
	 * Adapted from {@link ScaleBar}.
	 */
	private Point computeScalebarLocation(MontageItem item) {
		double barWidth = tool.getBarWidth();		
		String location = tool.getScalebarLocation();
		int fontSize = tool.getFontSize();
		int barHeightInPixels = (int) Math.floor(tool.getBarHeight()*fontSize);;
		int paddingWidth = tool.getPaddingWidth();
		
		Calibration cal = tool.getImp().getCalibration();
		int barWidthInPixels = (int)(barWidth/cal.pixelWidth);
		
		int width = tool.getImp().getWidth();
		int height = tool.getImp().getHeight();
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
		
		int xShift = item.getColumn()*tool.getImp().getWidth() + item.getColumn()*paddingWidth;
		int yShift = item.getRow()*tool.getImp().getHeight() + item.getRow()*paddingWidth;
		
		return new Point(xShift + x, yShift + y);
	}
	
	/**
	 * Adapted from {@link ScaleBar}.
	 */
	private void addScalebarToOverlay(MontageItem item, Overlay overlay) {
		// TODO Remove existing scalebar from overlay?
		// overlay.remove(SCALE_BAR);

		double barWidth = tool.getBarWidth();
		int fontSize = tool.getFontSize();
		int barHeightInPixels = (int) Math.floor(tool.getBarHeight()*fontSize);
		Color color = tool.getScalebarColor();

		Point scalebarLocation = computeScalebarLocation(item);		
		int x = scalebarLocation.x;
		int y = scalebarLocation.y;
		
		int fontType = boldText?Font.BOLD:Font.PLAIN;
		String face = serifFont?"Serif":"SanSerif";
		Font font = new Font(face, fontType, fontSize);
		String label = getLength(barWidth) + " "+ getUnits(tool.getImp());
		
		ImageProcessor ip = tool.getImp().getProcessor();
		ip.setFont(font);
		int textWidth = hideText?0:ip.getStringWidth(label);
		Calibration cal = tool.getImp().getCalibration();
		
		int barWidthInPixels = (int)(barWidth/cal.pixelWidth);
		Roi bar = new Roi(x, y, barWidthInPixels, barHeightInPixels);
		bar.setFillColor(color);
		bar.setStrokeColor(color);
		overlay.add(bar);
		
		int xoffset = (barWidthInPixels - textWidth)/2;
		int yoffset =  2*barHeightInPixels;
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

	@Override
	public void actionPerformed(ActionEvent e) {
		ImagePlus montage = compileMontage();
		
		montage.show();
	}

}
