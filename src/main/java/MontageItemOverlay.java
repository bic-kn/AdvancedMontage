import java.awt.Color;

public abstract class MontageItemOverlay {
	
	private Color color;

	public MontageItemOverlay(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
	
}