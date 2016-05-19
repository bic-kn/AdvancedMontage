import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JMenu;

/**
 * 
 */

/**
 * @author stefan
 *
 */
public class ImpMenu extends JMenu implements ItemListener {

	/**
	 * @throws HeadlessException
	 */
	public ImpMenu() throws HeadlessException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @throws HeadlessException
	 */
	public ImpMenu(String label) throws HeadlessException {
		super(label);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param label
	 * @param tearOff
	 * @throws HeadlessException
	 */
	public ImpMenu(String label, boolean tearOff) throws HeadlessException {
		super(label, tearOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

}
