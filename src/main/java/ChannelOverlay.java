/*-
 * #%L
 * Advanced Montage.
 * %%
 * Copyright (C) 2016 - 2018 Board of Regents of the University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
