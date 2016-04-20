import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;

import ij.process.LUT;

class MontageItemPopup extends PopupMenu {

	/**
	 * 
	 */
	private final MontagePanel montagePanel;

	/**
	 * @param montagePanel
	 */
	MontageItemPopup(MontagePanel montagePanel) {
		this.montagePanel = montagePanel;
	}

	public MontageItemPopup() {
		this.montagePanel = new MontagePanel(null);
	}

	MontageItem item;

	public void init() {
		this.item = (MontageItem) getParent();

		// Add "Clear" item
		MenuItem clearItem = new MenuItem("Clear");
		clearItem.setName("clearItem");
		clearItem.addActionListener(item);
		this.add(clearItem);

		// For all available channels in the image
		for (LUT lut : this.montagePanel.luts) {
			// TODO Check if an overlay exists for that LUT
			CheckboxMenuItem channelItem = new CheckboxMenuItem(MontageUtil.getLUTName(lut),
					item.overlaysContain(lut) ? true : false);
			channelItem.addItemListener(item);
			this.add(channelItem);
		}

		// Add "Composite" item
		MenuItem compositeItem = new MenuItem("Composite");
		compositeItem.setName("compositeItem");
		compositeItem.addActionListener(item);
		this.add(compositeItem);

		// Add "ROI" item
		CheckboxMenuItem roiItem = new CheckboxMenuItem("ROI", false);
		roiItem.setName("roiItem");
		roiItem.addItemListener(item);
		this.add(roiItem);

		// Add "Scalebar" item
		CheckboxMenuItem scalebarItem = new CheckboxMenuItem("Scalebar", false);
		scalebarItem.setName("scalebarItem");
		scalebarItem.addItemListener(item);
		this.add(scalebarItem);
	}

	public void update() {
		// TODO
	}
}