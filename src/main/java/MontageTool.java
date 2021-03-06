/*-
 * #%L
 * Advanced Montage.
 * %%
 * Copyright (C) 2016 - 2018 Board of Regents of the University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
// TODO Insert license header

import org.scijava.prefs.PrefService;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import fiji.tool.AbstractTool;
import fiji.tool.ToolToggleListener;
import fiji.tool.ToolWithOptions;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
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
	private List<ImagePlus> imps;
	
	private MontageFrame montageFrame; 
	private GenericDialog gd;

	// General
	private ActiveRoiPreference activeRoiPreference = ActiveRoiPreference.ROI;

	// Scalebar
	String fontName = "SansSerif";
	double fontSize = 42.0;
	double scalebarWidth = 10.0;
	double scalebarHeight = 0.25;
	String scalebarPosition = "Lower Right";
	Color scalebarColor = Color.WHITE;
	
	// ROI
	Color roiColor = Color.WHITE;
	
	// Padding
	int paddingWidth = 10;
	Color paddingColor = Color.WHITE;
	
	private PrefService prefService;
	
	/** Tile size in pixels */
	private static int TILE_SIZE = 20;
	
	/**
	 * TODO Documentation
	 */
	public MontageTool(final PrefService prefService) {
		super();

		this.prefService = prefService;
		
		activeRoiPreference = ActiveRoiPreference.valueOf(prefService.get("activeRoiPreference", "ROI"));

		fontName = prefService.get("scalebar.fontName", "SansSerif");
		fontSize = prefService.getDouble("scalebar.fontSize", 42.0);

		scalebarWidth = prefService.getDouble("scalebar.width", 10.0);
		scalebarHeight = prefService.getDouble("scalebar.height", 0.25);
		scalebarPosition = prefService.get("scalebar.position", "Lower Right");
		scalebarColor = getColor(prefService.get("scalebar.color", "white"));

		roiColor = getColor(prefService.get("roi.color", "white"));

		paddingWidth = prefService.getInt("padding.width", 10);
		paddingColor = getColor(prefService.get("padding.color", "white"));
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
		// update property
		prefService.put("activeRoiPreference", activeRoiPreference.toString());

		prefService.put("scalebar.fontName", fontName);
		prefService.put("scalebar.fontSize", fontSize);

		prefService.put("scalebar.width", scalebarWidth);
		prefService.put("scalebar.height", scalebarHeight);
		prefService.put("scalebar.position", scalebarPosition);
		prefService.put("scalebar.color", getColorName(scalebarColor));

		prefService.put("roi.color", getColorName(roiColor));

		prefService.put("padding.width", paddingWidth);
		prefService.put("padding.color", getColorName(paddingColor));
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
		return getAvailableLuts(getImp());
	}
	
	public LUT[] getAvailableLuts(final ImagePlus imagePlus) {
		return imagePlus.getLuts();
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

	public List<ImagePlus> getImps() {
		return imps;
	}

	public void setImps(List<ImagePlus> imps) {
		this.imps = imps;
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

	public void setPrefService(PrefService prefService) {
		this.prefService = prefService;
	}
	
}
