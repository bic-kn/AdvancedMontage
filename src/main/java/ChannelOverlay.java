// TODO Missing license header

import java.awt.Color;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ChannelOverlay extends MontageItemOverlay {

	private int channel;
	
	public ChannelOverlay(final ImagePlus imp, final int channel) {
		this(Color.WHITE, channel);
		
		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage) imp;
			ci.setC(channel);
			if (ci.getMode() == IJ.COMPOSITE) {
				Color c = ci.getChannelColor();
				if (Color.green.equals(c)) {
					c = new Color(0,180,0);
				}
				
				setColor(c);
			}
		}
	}
	
	/**
	 * 
	 * @param color
	 * @param channel
	 */
	public ChannelOverlay(Color color, int channel) {
		super(color);
		
		this.channel = channel;
	}
	
	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

}
