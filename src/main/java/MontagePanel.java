// TODO Missing license header

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
		
		// TODO Set the defaults
		ComponentMover cm = new ComponentMover();
		for (int i=0; i<ROWS*COLUMNS; i++) {
			MontageItem item;
			if (i < numberOfChannels) {
				MontageItemOverlay defaultChannelOverlay = MontageUtil.getOverlayForChannel(tool.getImp(), i+1);
				List<MontageItemOverlay> defaultOverlays = new ArrayList<>();
				defaultOverlays.add(defaultChannelOverlay);
				item = new MontageItem(tool, defaultOverlays);
			} else if (i == numberOfChannels) {
				List<MontageItemOverlay> defaultOverlays = new ArrayList<>();
				for (int channels = 1; channels <= numberOfChannels; channels++) {
					defaultOverlays.add(MontageUtil.getOverlayForChannel(tool.getImp(), channels));				
				}
				item = new MontageItem(tool, defaultOverlays);
			} else {
				item = new MontageItem(tool);
			}
			
			this.add(item);
//			cm.registerComponent(item);
		}
		
	    // TODO Set correct snap size
//	    cm.setSnapSize(new Dimension(40, 40));
	}
	
}
