// Missing license header.

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import ij.gui.Roi;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class RoiOverlay extends MontageItemOverlay implements ActionListener {

	private Roi[] rois;

	public RoiOverlay() {
		super(Color.DARK_GRAY);
	}

	/**
	 * @return the rois
	 */
	public Roi[] getRois() {
		return rois;
	}

	/**
	 * @param rois the rois to set
	 */
	public void setRois(Roi[] rois) {
		this.rois = rois;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem sourceMenuItem = (JMenuItem) e.getSource();
			switch (sourceMenuItem.getName()) {
			case "roiItem":
				setDrawn(true);
				break;
			case "clearItem":
				setDrawn(false);
				break;
			}
		}
	}

}
