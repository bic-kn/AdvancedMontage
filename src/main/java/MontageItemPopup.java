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
import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ij.ImagePlus;

class MontageItemPopup extends JPopupMenu implements ItemListener {

	private MontageTool tool;
	
	private boolean initialized = false;

	public MontageItemPopup(MontageItem item, MontageTool tool) {
		this.tool = tool;
		this.item = item;
		
		init();
	}

	private MontageItem item;

	private JMenuItem clearItem;
	private OverlayCheckBoxMenuItem roiItem;
	private OverlayCheckBoxMenuItem scalebarItem;
	private Map<JMenu, List<JCheckBoxMenuItem>> menuItems = new HashMap<>();
	
	private void init() {
		clearItem = new JMenuItem("Clear");
		clearItem.setName("clearItem");

		roiItem = new OverlayCheckBoxMenuItem("ROI", false);
		roiItem.setName("roiItem");

		scalebarItem = new OverlayCheckBoxMenuItem("Scalebar", false);
		scalebarItem.setName("scalebarItem");

		this.add(clearItem);
		
		for (ImagePlus imp : tool.getImps()) {
			ImpMenu impMenu = new ImpMenu(imp.getTitle());
			this.add(impMenu);
			
			JMenuItem compositeItem = new JMenuItem("Composite");
			compositeItem.setName("compositeItem");
			
			// For all available channels in the image
			List<JCheckBoxMenuItem> channelItems = new ArrayList<>();
			for (MontageItemOverlay itemOverlay : item.getOverlays()) {
				if (itemOverlay instanceof ChannelOverlay) {
					ChannelOverlay channelOverlay = (ChannelOverlay) itemOverlay;
					if (channelOverlay.getImp() == imp) {
						OverlayCheckBoxMenuItem channelItem = new OverlayCheckBoxMenuItem(channelOverlay.getNameForPopup());
						channelItems.add(channelItem);
						
						channelItem.addItemListener(channelOverlay); // TODO Dedicated Controller?
						channelItem.addItemListener(this); // TODO Dedicated Controller?
						channelOverlay.addOverlayListener(channelItem);
						
						// Make the channelOverlay listen to the clear and
						// composite menu entries
						compositeItem.addActionListener(channelOverlay);
						clearItem.addActionListener(channelOverlay);
						
						impMenu.add(channelItem);
					}
				} else if (itemOverlay instanceof RoiOverlay) {
					RoiOverlay roiOverlay = (RoiOverlay) itemOverlay;
					clearItem.addActionListener(roiOverlay);
					roiItem.addItemListener(roiOverlay);
					roiOverlay.addOverlayListener(roiItem);
				} else if (itemOverlay instanceof ScalebarOverlay) {
					ScalebarOverlay scalebarOverlay = (ScalebarOverlay) itemOverlay;
					clearItem.addActionListener(scalebarOverlay);
					scalebarItem.addItemListener(scalebarOverlay);
					scalebarOverlay.addOverlayListener(scalebarItem);
				}
			}
			menuItems.put(impMenu, channelItems);
			
			impMenu.add(compositeItem);
		}

		
		this.add(roiItem);
		this.add(scalebarItem);
		
		setInitialized(true);
	}

	public void update() {
		// TODO
	}

	/**
	 * @return the menuItems
	 */
	private Map<JMenu, List<JCheckBoxMenuItem>> getMenuItems() {
		return menuItems;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof OverlayCheckBoxMenuItem) {
			OverlayCheckBoxMenuItem checkbox = (OverlayCheckBoxMenuItem) e.getSource();
			
			if (e.getStateChange() == ItemEvent.SELECTED) {
				disableOtherSubmenus(checkbox);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				// TODO Refactor!
				for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : getMenuItems().entrySet()) {
					List<JCheckBoxMenuItem> items = entry.getValue();
					if (items.contains(checkbox)) {
						if (items.stream().noneMatch(x -> x.getState()==true)) {
							enableOtherSubmenus(checkbox);
						}
					}
				}
			}
		}
	}

	/**
	 * Enables all submenus that do not contain the input
	 * {@link CheckboxMenuItem}.
	 * 
	 * @param checkbox
	 */
	private void enableOtherSubmenus(OverlayCheckBoxMenuItem checkbox) {
		for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : getMenuItems().entrySet()) {
			if (!entry.getValue().contains(checkbox)) {
				entry.getKey().setEnabled(true);
			}
		}
	}

	/**
	 * Disables all submenus that do not contain the input
	 * {@link CheckboxMenuItem}.
	 * 
	 * @param checkbox
	 */
	private void disableOtherSubmenus(OverlayCheckBoxMenuItem checkbox) {
		for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : getMenuItems().entrySet()) {
			if (!entry.getValue().contains(checkbox)) {
				entry.getKey().setEnabled(false);
			}
		}
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	private void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
