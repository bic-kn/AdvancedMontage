// TODO Insert license header

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import fiji.tool.AbstractTool;
import fiji.tool.ToolToggleListener;
import fiji.tool.ToolWithOptions;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
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
	
	/** Tile size in pixels */
	private static int TILE_SIZE = 20;
	
	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getToolName() {
		return "Montage Tool";
	}

	@Override
	public String getToolIcon() {
		return "C000F00ffCfffT5e12MCf00F2244C0f0F6244Cf0fFa244Cff0F2644C0ffF6644";
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
		
		// Launch interactive mode
		super.run(arg);
	}

	@Override
	public void toolToggled(boolean enabled) {
		imp = WindowManager.getCurrentImage();
		
		if (imp == null) {
			// TODO Handle activation without open image
		}
		
		// Get active LUTs
		LUT[] luts = imp.getLuts();
		
		montageFrame = new MontageFrame(luts, this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOptionDialog() {
		// TODO Auto-generated method stub
		
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
		ImagePlus image = IJ.openImage("/Users/stefan/Dropbox/Konstanz/ImageJ Workshop 2016/2016/Examples/01/VH7.tif");
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
}
