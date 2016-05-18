// TODO Insert license header

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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

	/**
		 * @author stefan
		 *
		 */
	public enum ActiveRoiPreference {
		ROI,
		SCALEBAR;
	}

	private ImagePlus imp;

	private MontageFrame montageFrame; 
	private GenericDialog gd;
	
	// Scalebar
	String fontName = "SansSerif";
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

	private ActiveRoiPreference activeRoiPreference = ActiveRoiPreference.ROI;
	
	/** Tile size in pixels */
	private static int TILE_SIZE = 20;
	
	/**
	 * TODO Documentation
	 */
	public MontageTool() {
		super();

		Configurations configs = new Configurations();
		try {
			// TODO Remove hardcoded location
			Configuration config = configs.properties(new File("/home/stefan/.imagej/Advanced_Montage.properties"));

			// access configuration properties
			activeRoiPreference = ActiveRoiPreference.valueOf(config.getString("activeRoiPreference", "ROI"));

			fontName = config.getString("scalebar.fontName", "SansSerif");
			fontSize = config.getInt("scalebar.fontSize", 42);

			scalebarWidth = config.getDouble("scalebar.width", 10.0);
			scalebarHeight = config.getDouble("scalebar.height", 0.25);
			scalebarPosition = config.getString("scalebar.position", "Lower Right");
			scalebarColor = getColor(config.getString("scalebar.color", "white"));

			roiColor = getColor(config.getString("roi.color", "white"));

			paddingWidth = config.getInt("padding.width", 10);
			paddingColor = getColor(config.getString("padding.color", "white"));
		} catch (ConfigurationException cex) {
			// Values that cannot be read are filled with the default values
			// they have been assigned at declaration anyway.
			return;
		}
	}

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
		
		// General
		gd.addMessage("General Settings");
		gd.addChoice("Active ROI", new String[]{"ROI", "SCALEBAR"}, getActiveRoiPreference().toString());
		
		// Scalebar
		gd.addMessage("Scalebar Settings");
		gd.addStringField("Font", getFontName());
		gd.addNumericField("Font size", getFontSize(), 0);
		gd.addNumericField("Width", getBarWidth(), 1, 4, "[unit]");
		gd.addNumericField("Height", getBarHeight(), 3, 5, "[%]");
		gd.addChoice("Position", new String[]{"Lower Right", "Lower Left", "Upper Right", "Upper Left", "At Selection"}, getScalebarLocation());
		gd.addChoice("Color", availableColorsAsStrings(), getColorName(getScalebarColor()));
		
		// ROI
		gd.addMessage("ROI Settings");
		gd.addChoice("Color", availableColorsAsStrings(), getColorName(getRoiColor()));
		
		// Padding
		gd.addMessage("Padding");
		gd.addNumericField("Width", getPaddingWidth(), 0, 4, "[px]");
		gd.addChoice("Color", availableColorsAsStrings(), getColorName(getPaddingColor()));
	}

	@Override
	public void showOptionDialog() {
		initOptionDialog();
		
		gd.showDialog();
		
		if (!gd.wasCanceled()) {
			parseOptionsFromDialog(gd);
			persistOptions();
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
		
		// General
		String activeRoiPreferenceString = gd.getNextChoice();
		activeRoiPreference = ActiveRoiPreference.valueOf(activeRoiPreferenceString);
		
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
	 * TODO Documentation
	 */
	private void persistOptions() {
		Configurations configs = new Configurations();
		try {
			// obtain the configuration
			FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs
					.propertiesBuilder("/home/stefan/.imagej/Advanced_Montage.properties");
			PropertiesConfiguration config = builder.getConfiguration();

			// update property
			config.setProperty("activeRoiPreference", activeRoiPreference.toString());

			config.setProperty("scalebar.fontName", fontName);
			config.setProperty("scalebar.fontSize", fontSize);

			config.setProperty("scalebar.width", scalebarWidth);
			config.setProperty("scalebar.height", scalebarHeight);
			config.setProperty("scalebar.position", scalebarPosition);
			config.setProperty("scalebar.color", getColorName(scalebarColor));

			config.setProperty("roi.color", getColorName(roiColor));

			config.setProperty("padding.width", paddingWidth);
			config.setProperty("padding.color", getColorName(paddingColor));

			// save configuration
			builder.save();
		} catch (ConfigurationException cex) {
			// Something went wrong
		}
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
	
	private Set<String> availableColors = new HashSet<>();
	
	/**
	 * Generates a set of available default colors from the statically defined
	 * colors in {@code java.awt.Color}. This set does not contain duplicates.
	 * It is cached to avoid unnecessary calls to the Reflection API.
	 * 
	 * @return set of available default colors
	 */
	public String[] availableColorsAsStrings() {
		if (availableColors.isEmpty()) {
			try {
				Field[] fields = Class.forName("java.awt.Color").getFields();
				for (Field field : fields) {
					// Only handle static fields that are Color
					Object fieldObject = field.get(null);
					if (Modifier.isStatic(field.getModifiers()) && fieldObject instanceof Color) {
						availableColors.add(field.getName().toLowerCase());
					}
				}
			} catch (SecurityException | ClassNotFoundException | IllegalArgumentException
					| IllegalAccessException ex) {
				// TODO Add proper exception handling
				ex.printStackTrace();
			}
		}

		return availableColors.toArray(new String[availableColors.size()]);
	}
	
	/**
	 * Returns a {@link Color} instance for a provided String using the
	 * Reflection API.
	 * 
	 * @param color
	 * @return Name for the input. {@code null} if the color could not be found.
	 */
	private String getColorName(Color color) {
		try {
			Field[] fields = Class.forName("java.awt.Color").getFields();
			for (Field field : fields) {
				// Only handle static fields that are Color
				Object fieldObject = field.get(null);
				if (Modifier.isStatic(field.getModifiers()) && fieldObject instanceof Color && fieldObject.equals(color)) {
					return field.getName().toLowerCase();
				}
			}
		} catch (SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException ex) {
			// TODO Add proper exception handling
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns the String representation for a {@link Color} instance.
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
	
	// ------- getters and setters --------
	
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
	 * @return the scalebar color
	 */
	public Color getScalebarColor() {
		return scalebarColor;
	}

	/**
	 * @return the scalebar font size
	 */
	public int getFontSize() {
		// TODO Fix implementation
		return (int) fontSize;
	}

	/**
	 * @return the scalebar position
	 */
	public String getScalebarLocation() {
		return scalebarPosition;
	}

	/**
	 * @return the scalebar width
	 */
	public double getBarHeight() {
		return scalebarHeight;
	}

	/**
	 * @return the scalebar width
	 */
	public double getBarWidth() {
		return scalebarWidth;
	}
	
	/**
	 * @return the imp
	 */
	public ImagePlus getImp() {
		return imp;
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @param imp
	 */
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}

	/**
	 * TODO Documentation
	 * 
	 * @param montageFrame
	 */
	public void setFrame(MontageFrame montageFrame) {
		this.montageFrame = montageFrame;
	}
	
	// ------- unused events --------
	
	/**
	 * @return the fontName
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * @param fontName the fontName to set
	 */
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	/**
	 * @return the roiColor
	 */
	public Color getRoiColor() {
		return roiColor;
	}

	/**
	 * @param roiColor the roiColor to set
	 */
	public void setRoiColor(Color roiColor) {
		this.roiColor = roiColor;
	}

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
	
	// ------- main method --------
	
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
	 * TODO Documentation
	 * 
	 * @return
	 */
	public ActiveRoiPreference getActiveRoiPreference() {
		return activeRoiPreference;
	}
	
}
