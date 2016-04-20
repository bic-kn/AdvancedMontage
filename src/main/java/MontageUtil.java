// TODO Missing license header

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontageUtil {

	/** TODO Move to a central controller */
	private static Map<Integer, MontageItemOverlay> overlayForChannelMap = new HashMap<>();
	
	public static String getLUTName(LUT lut) {	
		return lut.toString();
	}
	
	public static MontageItemOverlay getOverlayForChannel(final ImagePlus imp, final int channel) {
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
	
}
