// Missing license header.

import java.awt.Color;

import ij.gui.Roi;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class RoiOverlay extends MontageItemOverlay {

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

}
