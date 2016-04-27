import java.awt.Color;

public abstract class MontageItemOverlay {
	
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
	}
	
}