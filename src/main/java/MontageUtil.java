// TODO Missing license header

import java.awt.Color;

import ij.plugin.Colors;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontageUtil {
	
	public static String getLUTName(final LUT lut, final int channel) {	
		String channelName = "Channel " + channel;
		Color maxColor = new Color(lut.getRGB(255));
		String lutName = Colors.colorToString2(maxColor);
		
		// TODO Can we somehow use the LUTService?
		if (maxColor.equals(Color.WHITE)) {
			// colorToString2 does not return "Gray" but "#808080"
//			lutName = Colors.colorToString2(Color.GRAY);
			// TODO Handle special cases
			lutName = "Gray";
		}
		
		return channelName + " - " + lutName;
	}

}
