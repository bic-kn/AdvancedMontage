// TODO Missing license header

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import ij.ImagePlus;
import ij.plugin.Colors;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ChannelOverlay extends MontageItemOverlay implements ActionListener {

	/** The channel that {@code this} overlay represents */
	private int channel;

	/** The {@link ImagePlus} that {@code this} overlay represents. */
	private ImagePlus imp;

	/**
	 * Constructs a {@code ChannelOverlay} by extracting the LUT for a
	 * specific channel from an input {@link ImagePlus}.
	 * 
	 * @param imp
	 *            {@link ImagePlus} from which the LUT's color at value 255 is
	 *            extracted
	 * @param channel
	 *            the channel to look up
	 */
	public ChannelOverlay(final ImagePlus imp, final int channel) {
		this(Color.WHITE, channel);

		LUT[] luts = imp.getLuts();
		Color c = new Color(luts[channel].getRGB(255));

		setColor(c);

		this.setImp(imp);
	}

	/**
	 * Constructs a {@code ChannelOverlay} for a channel with a specified color.
	 * 
	 * @param color
	 *            the {@link Color} to set
	 * @param channel
	 *            the channel to look up
	 */
	public ChannelOverlay(final Color color, final int channel) {
		super(color);

		this.channel = channel;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @return the imp
	 */
	public ImagePlus getImp() {
		return imp;
	}

	/**
	 * @param imp the imp to set
	 */
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}

	public String getNameForPopup() {
		String channelName = "Channel " + getChannel();
		Color maxColor = getColor();
		String lutName = Colors.colorToString2(maxColor);

		// TODO Can we somehow use the LUTService?
		if (maxColor.equals(Color.WHITE)) {
			// colorToString2 does not return "Gray" but "#808080"
			// TODO Handle special cases
			lutName = "Gray";
		}

		return channelName + " - " + lutName;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem sourceMenuItem = (JMenuItem) e.getSource();
			switch (sourceMenuItem.getName()) {
			case "compositeItem":
				setDrawn(true);
				break;
			case "clearItem":
				setDrawn(false);
				break;
			}
		}
	}

}
