// TODO Missing license header

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ij.ImagePlus;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontagePanel extends JPanel implements ComponentListener {

	private final static int ROWS = 4;
	private final static int COLUMNS = 4;
	private MontageTool tool;
	private ComponentMover cm;
	
	// TODO Get correct initial sizes
	private int tileWidth = 40;
	private int tileHeight = 30;
	
	public MontagePanel(MontageTool tool) {
		this.tool = tool;
		
		placeComponents();
		
		addComponentListener(this);
	}
	
	/**
	 * Places {@link MontageItem}s in a grid and makes them draggable via
	 * {@link ComponentMover}.
	 * 
	 * TODO Improve dragging functionality: snapping should switch items
	 */
	private void placeComponents() {
		this.setLayout(new GridLayout(ROWS,COLUMNS));

		int numberOfChannels = tool.getImp().getNChannels();

		cm = new ComponentMover();
		for (int i=0; i<ROWS*COLUMNS; i++) {
			List<MontageItemOverlay> defaultOverlays = new ArrayList<>();

			// Add channels
			for (ImagePlus imp : tool.getImps()) {
				for (int c = 0; c < imp.getNChannels(); c++) {
					ChannelOverlay defaultChannelOverlay = new ChannelOverlay(imp, c);
					defaultOverlays.add(defaultChannelOverlay);
				}
			}

			// Add ROI
			RoiOverlay roiOverlay = new RoiOverlay();
			defaultOverlays.add(roiOverlay);
			
			// Add scalebar
			ScalebarOverlay scaleOverlay = new ScalebarOverlay();
			defaultOverlays.add(scaleOverlay);
			
			MontageItem item = new MontageItem(tool, defaultOverlays);
			MontageItemPopup montageItemPopup = new MontageItemPopup(item, tool);
			item.setMenu(montageItemPopup);

			// Defaults: initialize each channel individually plus a composite
			if (i <= numberOfChannels) {
				initializeOverlay(i, numberOfChannels, defaultOverlays);
			}

			this.add(item);
			cm.registerComponent(item);
		}
	}

	/**
	 * TODO Documentation
	 * 
	 * @param tileId
	 * @param numberOfChannels
	 * @param defaultOverlays
	 */
	private void initializeOverlay(int tileId, int numberOfChannels,
			List<MontageItemOverlay> defaultOverlays) {
		for (MontageItemOverlay itemOverlay : defaultOverlays) {
			if (itemOverlay instanceof ChannelOverlay) {
				ChannelOverlay channelOverlay = (ChannelOverlay) itemOverlay;
				// Use the active window (tool.getImp()) for defauls
				if (channelOverlay.getImp() == tool.getImp()) {
					if (tileId < numberOfChannels && channelOverlay.getChannel() == tileId) {
						// Draw exactly one channel
						channelOverlay.setDrawn(true);
						break;
					} else if (tileId == numberOfChannels && channelOverlay.getChannel() < numberOfChannels) {
						// In the composite tile, all available channels are drawn
						channelOverlay.setDrawn(true);
					}
				}
			}
		}
	}

	public void setSnapSize() {
		cm.setSnapSize(new Dimension(getComponents()[0].getWidth(), getComponents()[0].getHeight()));
	}
	
	private MontageItem createMontageItem() {
		List<MontageItemOverlay> defaultOverlays = new ArrayList<>();
		
		// Add channels
		for (int c = 0; c < tool.getImp().getNChannels(); c++) {
			ChannelOverlay defaultChannelOverlay = new ChannelOverlay(tool.getColorForChannel(c+1), c+1);
			defaultOverlays.add(defaultChannelOverlay);
		}
		
		// Add ROI
		RoiOverlay roiOverlay = new RoiOverlay();
		defaultOverlays.add(roiOverlay);
		
		// Add scalebar
		ScalebarOverlay scaleOverlay = new ScalebarOverlay();
		defaultOverlays.add(scaleOverlay);
		
		return new MontageItem(tool, defaultOverlays);
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		setSnapSize();
		
		int tilesInRow = getWidth() / tileWidth;
		int tilesInColumn = getHeight() / tileHeight;

		while (getComponentCount() < tilesInColumn * tilesInRow) {
			this.add(createMontageItem());
		}
		
		while (getComponentCount() > tilesInColumn * tilesInRow) {
			this.remove(getComponentCount()-1);
		}
		
		GridLayout newLayout = new GridLayout(tilesInColumn, tilesInRow);
		this.setLayout(newLayout);
	}

	@Override
	public void componentMoved(ComponentEvent e) { /* NB */	}

	@Override
	public void componentShown(ComponentEvent e) {
		tileWidth = getComponents()[0].getWidth();
		tileHeight = getComponents()[0].getHeight();
	}

	@Override
	public void componentHidden(ComponentEvent e) { /* NB */ }
}
