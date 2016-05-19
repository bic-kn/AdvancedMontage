import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.security.cert.CertPathChecker;
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
import ij.process.LUT;

class MontageItemPopup extends JPopupMenu implements ItemListener {

	private MontageTool tool;
	
	public MontageItemPopup(MontageTool tool) {
		this.tool = tool;
		
		// FIXME Call init()
	}

	private MontageItem item;

	private JMenuItem clearItem;
	private JMenuItem compositeItem;
	private JCheckBoxMenuItem roiItem;
	private JCheckBoxMenuItem scalebarItem;
	private Map<JMenu, List<JCheckBoxMenuItem>> menuItems = new HashMap<>();
	
	public void init() {
		this.item = (MontageItem) getParent();

		clearItem = new JMenuItem("Clear");
		clearItem.setName("clearItem");
		clearItem.addActionListener(item);
		this.add(clearItem);

		for (ImagePlus imp : tool.getImps()) {
			ImpMenu impMenu = new ImpMenu(imp.getTitle());
			this.add(impMenu);
			
			// For all available channels in the image
			List<JCheckBoxMenuItem> channelItems = new ArrayList<>();
			for (MontageItemOverlay itemOverlay : item.getOverlays()) {
				if (itemOverlay instanceof ChannelOverlay) {
					ChannelOverlay channelOverlay = (ChannelOverlay) itemOverlay;
					if (channelOverlay.getImp() == imp) {
						ChannelMenuItem channelItem = new ChannelMenuItem(channelOverlay.getNameForPopup(),
								channelOverlay.isDrawn());
						channelItems.add(channelItem);
						channelItem.addItemListener(channelOverlay);
						channelItem.addItemListener(this);
						channelOverlay.addOverlayListener(channelItem);
						
						// Force sync between overlay and menu item
						channelOverlay.setDrawn(channelOverlay.isDrawn());
						
						impMenu.add(channelItem);
					}
				}
			}
			menuItems.put(impMenu, channelItems);
			
			compositeItem = new JMenuItem("Composite");
			compositeItem.setName("compositeItem");
			compositeItem.addActionListener(item);
			impMenu.add(compositeItem);
		}

		roiItem = new JCheckBoxMenuItem("ROI", false);
		roiItem.setName("roiItem");
		roiItem.addItemListener(item);
		this.add(roiItem);

		scalebarItem = new JCheckBoxMenuItem("Scalebar", false);
		scalebarItem.setName("scalebarItem");
		scalebarItem.addItemListener(item);
		this.add(scalebarItem);
	}

	public void update() {
		// TODO
	}
	
	public void clearMenu() {
		clearChannels();
		clearRoi();
		clearScalebar();
	}

	private void clearChannels() {
		for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : menuItems.entrySet()) {
			for (JCheckBoxMenuItem channelItem : entry.getValue()) {
				channelItem.setState(false);
			}
		}
	}
	
	private void clearRoi() {
		roiItem.setState(false);
	}
	
	private void clearScalebar() {
		scalebarItem.setState(false);
	}

	public void composite() {
		for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : menuItems.entrySet()) {
			for (JCheckBoxMenuItem channelItem : entry.getValue()) {
				channelItem.setState(true);
			}
		}
	}

	/**
	 * @return the menuItems
	 */
	private Map<JMenu, List<JCheckBoxMenuItem>> getMenuItems() {
		return menuItems;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof CheckboxMenuItem) {
			CheckboxMenuItem checkbox = (CheckboxMenuItem) e.getSource();
			
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
	private void enableOtherSubmenus(CheckboxMenuItem checkbox) {
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
	private void disableOtherSubmenus(CheckboxMenuItem checkbox) {
		for (Entry<JMenu, List<JCheckBoxMenuItem>> entry : getMenuItems().entrySet()) {
			if (!entry.getValue().contains(checkbox)) {
				entry.getKey().setEnabled(false);
			}
		}
	}

}