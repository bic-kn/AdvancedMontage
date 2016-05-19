// TODO Missing license header

import java.awt.CheckboxMenuItem;
import java.awt.HeadlessException;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ChannelMenuItem extends CheckboxMenuItem implements OverlayListener {

	/**
	 * @throws HeadlessException
	 */
	public ChannelMenuItem() throws HeadlessException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @throws HeadlessException
	 */
	public ChannelMenuItem(String label) throws HeadlessException {
		super(label);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @param state
	 * @throws HeadlessException
	 */
	public ChannelMenuItem(String label, boolean state) throws HeadlessException {
		super(label, state);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void overlayChanged(OverlayChangeEvent e) {
		setState(e.getSource().isDrawn());
	}

}
