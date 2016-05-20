// TODO Missing license header

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ScalebarOverlay extends MontageItemOverlay implements ActionListener {

	public ScalebarOverlay() {
		super(Color.DARK_GRAY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		super.actionPerformed(e);
//		consume in super.actionPerformed for clearItem
		
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem sourceMenuItem = (JMenuItem) e.getSource();
			switch (sourceMenuItem.getName()) {
			case "scalebarItem":
				setDrawn(true);
				break;
			case "clearItem":
				setDrawn(false);
				break;
			}
		}
	}

}
