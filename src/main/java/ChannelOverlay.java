// TODO Missing license header

import java.awt.Color;

import ij.ImagePlus;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ChannelOverlay extends MontageItemOverlay {

	private int channel;
	
	public ChannelOverlay(final ImagePlus imp, final int channel) {
		this(Color.WHITE, channel);
		
		LUT[] luts = imp.getLuts();
		Color c = new Color(luts[channel].getRGB(255));
		
		// TODO If necessary, implement fix for green color
//			if (Color.green.equals(c)) {
//				c = new Color(0,180,0);
//			}
			
		setColor(c);
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
