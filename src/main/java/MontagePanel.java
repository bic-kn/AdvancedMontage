// TODO Missing license header

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ij.ImagePlus;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontagePanel extends JPanel {

	private final static int ROWS = 4;
	private final static int COLUMNS = 4;
	private MontageTool tool;
	ComponentMover cm;
	
	public MontagePanel(MontageTool tool) {
		this.tool = tool;
		
		placeComponents();
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

					// Set the defaults according to the active image (i.e. tool.getImp())
					if (imp == tool.getImp()) {
						if (i == c || i == numberOfChannels) {
							defaultChannelOverlay.setDrawn(true);
						}
					}
				}
			}

			// Add ROI
			RoiOverlay roiOverlay = new RoiOverlay();
			defaultOverlays.add(roiOverlay);
			
			// Add scalebar
			ScalebarOverlay scaleOverlay = new ScalebarOverlay();
			defaultOverlays.add(scaleOverlay);
			
			MontageItem item = new MontageItem(tool, defaultOverlays);

			this.add(item);
			cm.registerComponent(item);
		}
	}

	public void setSnapSize() {
		cm.setSnapSize(new Dimension(getComponents()[0].getWidth(), getComponents()[0].getHeight()));
	}
}
