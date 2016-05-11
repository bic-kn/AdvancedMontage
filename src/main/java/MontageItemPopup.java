import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.process.LUT;

class MontageItemPopup extends PopupMenu {

	private MontageTool tool;
	
	public MontageItemPopup(MontageTool tool) {
		this.tool = tool;
		
		// FIXME Call init()
	}

	private MontageItem item;
	private MenuItem clearItem;
	private MenuItem compositeItem;
	private CheckboxMenuItem roiItem;
	private CheckboxMenuItem scalebarItem;
	private List<CheckboxMenuItem> channelItems;
	
	public void init() {
		this.item = (MontageItem) getParent();

		clearItem = new MenuItem("Clear");
		clearItem.setName("clearItem");
		clearItem.addActionListener(item);
		this.add(clearItem);

		// TODO Add submenu for each open image
		for (ImagePlus imp : tool.getImps()) {
			Menu impMenu = new Menu(imp.getTitle());
			this.add(impMenu);
			
			// For all available channels in the image
			channelItems = new ArrayList<>();
			int i = 1;
			for (LUT lut : tool.getAvailableLuts(imp)) {
				// TODO Check if an overlay exists for that LUT
				CheckboxMenuItem channelItem = new CheckboxMenuItem(MontageUtil.getLUTName(lut, i),
						item.overlaysContain(lut) ? true : false);
				if (item.overlayForChannel(i-1).isDrawn()) {
					channelItem.setState(true);
				}
				channelItems.add(channelItem);
				channelItem.setName("channel-"+i++);
				channelItem.addItemListener(item);
				impMenu.add(channelItem);
			}
			
			compositeItem = new MenuItem("Composite");
			compositeItem.setName("compositeItem");
			compositeItem.addActionListener(item);
			impMenu.add(compositeItem);
		}

		roiItem = new CheckboxMenuItem("ROI", false);
		roiItem.setName("roiItem");
		roiItem.addItemListener(item);
		this.add(roiItem);

		scalebarItem = new CheckboxMenuItem("Scalebar", false);
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
		for (CheckboxMenuItem channelItem : channelItems) {
			channelItem.setState(false);
		}
	}
	
	private void clearRoi() {
		roiItem.setState(false);
	}
	
	private void clearScalebar() {
		scalebarItem.setState(false);
	}

	public void composite() {
		for (CheckboxMenuItem channelItem : channelItems) {
			channelItem.setState(true);
		}
	}
}