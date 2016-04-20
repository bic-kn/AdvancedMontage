// TODO Missing license header

import java.awt.Color;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ChannelOverlay extends MontageItemOverlay {

	private int channel;

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
