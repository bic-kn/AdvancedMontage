import java.awt.Color;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class MontageItemOverlay implements OverlayListenable {
	
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

}