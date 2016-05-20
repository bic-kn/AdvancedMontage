import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class MontageItemOverlay implements OverlayListenable {
	
	List<OverlayListener> listeners = new LinkedList<>();
	
	private Color color;
	private boolean drawn = false;
	
	public MontageItemOverlay(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isDrawn() {
		return drawn;
	}

	public void setDrawn(boolean drawn) {
		this.drawn = drawn;
		listeners.forEach(l -> l.overlayChanged(new OverlayChangeEvent(this) { /* NB */ }));
	}

	@Override
	public void addOverlayListener(OverlayListener l) {
		listeners.add(l);
	}

	@Override
	public void removeOverlayListener(OverlayListener l) {
		listeners.remove(l);
	}

}