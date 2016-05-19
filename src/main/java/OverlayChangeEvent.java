/**
 * 
 */

/**
 * @author stefan
 *
 */
public abstract class OverlayChangeEvent {

	MontageItemOverlay source;

	/**
	 * @param source
	 */
	public OverlayChangeEvent(MontageItemOverlay source) {
		this.source = source;
	}

	public MontageItemOverlay getSource() {
		return source;
	}

}
