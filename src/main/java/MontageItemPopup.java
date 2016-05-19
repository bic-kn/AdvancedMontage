import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
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

class MontageItemPopup extends JPopupMenu {

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
			JMenu impMenu = new JMenu(imp.getTitle());
			this.add(impMenu);
			
			// For all available channels in the image
			List<JCheckBoxMenuItem> channelItems = new ArrayList<>();
			for (MontageItemOverlay itemOverlay : item.getOverlays()) {
				if (itemOverlay instanceof ChannelOverlay) {
					ChannelOverlay channelOverlay = (ChannelOverlay) itemOverlay;
					if (channelOverlay.getImp() == imp) {
						JCheckBoxMenuItem channelItem = new JCheckBoxMenuItem(channelOverlay.getNameForPopup(),
								channelOverlay.isDrawn());
						channelItems.add(channelItem);
						channelItem.addItemListener(channelOverlay);
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
}