// TODO Missing license header

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontagePanel extends JPanel {

	protected LUT[] luts;
	private final static int ROWS = 4;
	private final static int COLUMNS = 4;
	
	public MontagePanel(LUT[] luts) {
		this.luts = luts;
		
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

		// TODO Set the defaults
		ComponentMover cm = new ComponentMover();
		for (int i=0; i<ROWS*COLUMNS; i++) {
			MontageItem item = new MontageItem();
			this.add(item);
			cm.registerComponent(item);
		}
	    
	    // TODO Set correct snap size
	    cm.setSnapSize(new Dimension(40, 40));
	}
	
}
