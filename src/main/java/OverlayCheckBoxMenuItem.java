// TODO Missing license header

import java.awt.HeadlessException;
import javax.swing.JCheckBoxMenuItem;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class OverlayCheckBoxMenuItem extends JCheckBoxMenuItem implements OverlayListener {
	
	/**
	 * @throws HeadlessException
	 */
	public OverlayCheckBoxMenuItem() throws HeadlessException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @throws HeadlessException
	 */
	public OverlayCheckBoxMenuItem(String label) throws HeadlessException {
		super(label);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @param state
	 * @throws HeadlessException
	 */
	public OverlayCheckBoxMenuItem(String label, boolean state) throws HeadlessException {
		super(label, state);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void overlayChanged(OverlayChangeEvent e) {
		setSelected(e.getSource().isDrawn());
	}

}
