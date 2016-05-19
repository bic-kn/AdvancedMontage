// TODO Missing license header

import java.util.LinkedList;
import java.util.List;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public interface OverlayListenable {

	List<OverlayListener> listeners = new LinkedList<>();
	
	public default void addOverlayListener(OverlayListener l) {
		listeners.add(l);
	}

	public default void removeOverlayListener(OverlayListener l) {
		listeners.remove(l);
	}

}
