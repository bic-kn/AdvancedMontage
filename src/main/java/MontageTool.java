// TODO Insert license header

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import fiji.tool.AbstractTool;
import fiji.tool.ToolToggleListener;
import fiji.tool.ToolWithOptions;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * TODO Implement context menu for tool. Likely that
 * {@link AbstractTool#toolbarMouseListener} needs changes for that to handle
 * right clicks on the Toolbar.
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
@SuppressWarnings("javadoc")
public class MontageTool extends AbstractTool
		implements MouseMotionListener, MouseListener, ToolToggleListener,
		KeyListener, FocusListener, ToolWithOptions {

	private ImagePlus imp;

	private MontageFrame montageFrame; 
	private GenericDialog gd;
	
	// Scalebar
	String fontName;
	double fontSize = 42;
	double scalebarWidth = 10.0;
	double scalebarHeight = 0.25;
	String scalebarPosition = "Lower Right";
	Color scalebarColor = Color.WHITE;
	
	// ROI
	Color roiColor = Color.WHITE;
	
	// Padding
	int paddingWidth = 10;
	Color paddingColor = Color.WHITE;
	
	/** Tile size in pixels */
	private static int TILE_SIZE = 20;
	
	@Override
	public void run(String arg) {				
		// getToolId() returns -1 if no tool with the given name is found
		Toolbar toolbar = Toolbar.getInstance();		
		if (toolbar.getToolId(getToolName()) >= 0) {
			return;
		}
		
		// Let's add our tool as focus listener
		WindowManager.getCurrentWindow().addFocusListener(this);
		
		// Init options dialog
		initOptionDialog();
		
		// Launch interactive mode
		super.run(arg);
	}

	@Override
	public String getToolName() {
		return "Montage Tool";
	}

	@Override
	public String getToolIcon() {
		return "C000F00ffCfffT5e12MCf00F2244C0f0F6244Cf0fFa244Cff0F2644C0ffF6644";
	}
	
	private void initOptionDialog() {
		gd = new GenericDialog("Montage Options");
		
		// Time Stamp
		// TODO Implement properly
//		gd.addMessage("Time Stamp Settings");
//		gd.addChoice("Position", new String[]{"Lower Right", "Lower Left"}, "Lower Right");
		
		// Event Stamp
		// TODO Implement properly
//		gd.addMessage("Event Stamp Settings");
//		gd.addChoice("Position", new String[]{"Lower Right", "Lower Left"}, "Lower Right");
		
		// Scalebar
		gd.addMessage("Scalebar Settings");
		gd.addStringField("Font", "SansSerif");
		gd.addNumericField("Font size", getFontSize(), 0);
		gd.addNumericField("Width", getBarWidth(), 1, 4, "[unit]");
		gd.addNumericField("Height", getBarHeight(), 3, 5, "[%]");
		gd.addChoice("Position", new String[]{"Lower Right", "Lower Left", "Upper Right", "Upper Left", "At Selection"}, getScalebarLocation());
		gd.addChoice("Color", availableColorsAsStrings(), "White");
		
		// ROI
		gd.addMessage("ROI Settings");
		gd.addChoice("Color", availableColorsAsStrings(), "White");
		
		// Padding
		gd.addMessage("Padding");
		gd.addNumericField("Width", getPaddingWidth(), 0, 4, "[px]");
		gd.addChoice("Color", availableColorsAsStrings(), "White");
	}

	@Override
	public void showOptionDialog() {
		initOptionDialog();
		
		gd.showDialog();
		
		if (!gd.wasCanceled()) {
			parseOptionsFromDialog(gd);
		}
	}
	
	@Override
	public void toolToggled(boolean enabled) {
		imp = WindowManager.getCurrentImage();
		
		if (imp == null) {
			return;
		}
	}

	/**
	 * Parses options from a {@link GenericDialog}. Is not responsible for
	 * setting options.
	 * 
	 * TODO Improve documentation
	 * 
	 * @param gd
	 */
	private void parseOptionsFromDialog(GenericDialog gd) {
		// TODO Time Stamp
		
		// TODO Event Stamp
		
		// Scalebar
		fontName = gd.getNextString();
		fontSize = gd.getNextNumber();
		scalebarWidth = gd.getNextNumber();
		scalebarHeight = gd.getNextNumber();
		scalebarPosition = gd.getNextChoice();
		String scalebarColorString = gd.getNextChoice().toLowerCase();
		scalebarColor = getColor(scalebarColorString);

		// ROI
		String roiColorString = gd.getNextChoice().toLowerCase();
		roiColor = getColor(roiColorString);
		
		// Padding
		paddingWidth = (int) Math.floor(gd.getNextNumber());
		String paddingColorString = gd.getNextChoice();
		paddingColor = getColor(paddingColorString);
	}

	/**
	 * Returns a {@link Color} instance for a provided String using the
	 * Reflection API.
	 * 
	 * @param colorString
	 *            color name
	 * @return {@link Color} instance denoted by input
	 */
	private Color getColor(String colorString) {
		Color color = null;
		try {
		    Field field = Class.forName("java.awt.Color").getField(colorString.toLowerCase());
		    color = (Color) field.get(null);
		} catch (Exception e) {
			color = Color.WHITE; // Field not defined -> default
		}
		
		return color;
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = MontageTool.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("/home/stefan/Dropbox/Konstanz/ImageJ Workshop 2016/2016/Examples/01/VH7.tif");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
	
	/**
	 * @return the imp
	 */
	public ImagePlus getImp() {
		return imp;
	}

	public Collection<MontageItem> getMontageItems() {
		Component[] components = montageFrame.getPanel().getComponents();
		Collection<MontageItem> montageItems = new LinkedList<>();
		
		for (Component component : components) {
			if (component instanceof MontageItem) {
				montageItems.add((MontageItem) component);
			}
		}
		
		return montageItems;
	}

	public LUT[] getAvailableLuts() {
		return getImp().getLuts();
	}
	
	/** Compute channel/color mapping only once. */
	private Map<Integer, Color> channelToColor = new HashMap<>();

	/**
	 * TODO Documentation
	 * 
	 * @param channel 1-based channel number
	 * @return color from LUT for channel number
	 */
	public Color getColorForChannel(final int channel) {
		if (channelToColor.get(channel) != null) {
			return channelToColor.get(channel);
		}

		LUT[] luts = getAvailableLuts();
		Color c = new Color(luts[channel-1].getRGB(255));

		// TODO If necessary, implement fix for green color
//			if (Color.green.equals(c)) {
//				c = new Color(0,180,0);
//			}

		channelToColor.put(channel, c);
		return c;
	}

	/** Cache for channel number to {@link ChannelOverlay} mapping. */
	private Map<Integer, MontageItemOverlay> overlayForChannelMap = new HashMap<>();

	public MontageItemOverlay getOverlayForChannel(final int channel) {
		if (overlayForChannelMap.get(channel) != null) {
			return overlayForChannelMap.get(channel);
		}
		
		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage) imp;
			ci.setC(channel);
			if (ci.getMode() == IJ.COMPOSITE) {
				Color c = ci.getChannelColor();
				if (Color.green.equals(c)) {
					c = new Color(0,180,0);
				}
				
				MontageItemOverlay overlay = new ChannelOverlay(c, channel);
				overlayForChannelMap.put(channel, overlay);
				return overlay;
			}
		}
		
		MontageItemOverlay overlay = new ChannelOverlay(Color.WHITE, channel);
		overlayForChannelMap.put(channel, overlay);
		return overlay;
	}

	/**
	 * @return the padding width
	 */
	public int getPaddingWidth() {
		return paddingWidth;
	}

	/**
	 * @return the padding color
	 */
	public Color getPaddingColor() {
		return paddingColor;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return the scalebar color
	 */
	public Color getScalebarColor() {
		return scalebarColor;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return the scalebare font size
	 */
	public int getFontSize() {
		// TODO Fix implementation
		return (int) fontSize;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return the scalebar position
	 */
	public String getScalebarLocation() {
		return scalebarPosition;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return the scalebar width
	 */
	public double getBarHeight() {
		return scalebarHeight;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return the scalebar width
	 */
	public double getBarWidth() {
		return scalebarWidth;
	}
	
	// ------- unused events --------
	
	@Override
	public void focusGained(FocusEvent e) { /* NB */ }

	@Override
	public void focusLost(FocusEvent e) { /* NB */ }

	@Override
	public void keyTyped(KeyEvent e) { /* NB */ }

	@Override
	public void keyPressed(KeyEvent e) { /* NB */ }

	@Override
	public void keyReleased(KeyEvent e) { /* NB */ }
	
	@Override
	public void mouseClicked(MouseEvent e) { /* NB */ }

	@Override
	public void mousePressed(MouseEvent e) { /* NB */ }

	@Override
	public void mouseReleased(MouseEvent e) { /* NB */ }

	@Override
	public void mouseEntered(MouseEvent e) { /* NB */ }

	@Override
	public void mouseExited(MouseEvent e) { /* NB */ }

	@Override
	public void mouseDragged(MouseEvent e) { /* NB */ }

	@Override
	public void mouseMoved(MouseEvent e) { /* NB */ }

	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}

	public void setFrame(MontageFrame montageFrame) {
		this.montageFrame = montageFrame;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public static String[] availableColorsAsStrings() {
		return new String[]{"White", "Black", "Gray"};
	}
	
}
