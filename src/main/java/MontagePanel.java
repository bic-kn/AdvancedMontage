// TODO Missing license header

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

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
			
			MontageItem item = new MontageItem(tool, defaultOverlays);

			if (i < numberOfChannels) {
				// Only one channel at the position
				defaultOverlays.get(i).setDrawn(true);
			} else if (i == numberOfChannels) {
				// Composite
				for (int j=0; j<numberOfChannels; j++) {
					defaultOverlays.get(j).setDrawn(true);
				}
			}
			
			this.add(item);
			cm.registerComponent(item);
		}
	}

	public void setSnapSize() {
		cm.setSnapSize(new Dimension(getComponents()[0].getWidth(), getComponents()[0].getHeight()));
	}
}
