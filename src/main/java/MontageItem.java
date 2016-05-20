// TODO Missing license header

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.border.Border;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
class MontageItem extends JButton implements ItemListener {

	private List<MontageItemOverlay> overlays = new ArrayList<>();
	private MontageItemPopup menu;
	private MontageTool tool;
	
	public MontageItem(MontageTool tool, List<MontageItemOverlay> overlays) {
		this(tool);
		
		this.overlays.addAll(overlays);
	}
	
	public MontageItem(MontageTool tool) {
		this.tool = tool;
		
		this.setBackground(new Color(242, 242, 242));
		this.setContentAreaFilled(false);
		this.setBorderPainted(true);
		this.setBorder(new MontagePanelItemBorder());

		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) { /* Not used */ }

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPop(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPop(e);
				}
			}

			private void doPop(MouseEvent e) {
				if (!menu.isInitialized()) {
					add(menu);
				}

				menu.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mouseEntered(MouseEvent e) { /* Not used */ }

			@Override
			public void mouseExited(MouseEvent e) { /* Not used */ }

		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);

		g.setColor(new Color(217, 217, 217));
		g.fillRect(3, 3, getWidth()-6, getHeight()-6);
		
		// TODO Move to a central location
		int overlayHeight = 5;
		int overlayElementCount = 0;

		// Draw rectangles on the button in the order of overlays
		for (MontageItemOverlay overlayItem : overlays) {
			if (overlayItem instanceof ChannelOverlay && overlayItem.isDrawn()) {
				g.setColor(overlayItem.getColor());
				g.fillRect(overlayHeight,
						overlayElementCount * overlayHeight
								+ (overlayElementCount + 1) * overlayHeight,
						this.getWidth() - 2 * overlayHeight, overlayHeight);
				overlayElementCount++;
			}
			
			if (overlayItem instanceof RoiOverlay && overlayItem.isDrawn()) {
				g.setColor(overlayItem.getColor());
				// TODO Improve computation of position
				g.drawString("R", 2, this.getHeight() - 2);
			}
			
			if (overlayItem instanceof ScalebarOverlay && overlayItem.isDrawn()) {
				g.setColor(overlayItem.getColor());
				// TODO Improve computation of position
				g.drawString("S", this.getWidth()-10, this.getHeight() - 2);
			}
		}
	}

	public boolean overlaysContain(LUT lut) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean overlayContainsScalebar() {
		// TODO
		return false;
	}

	public boolean overlayContainsRoi() {
		// TODO
		return false;
	}

	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	private RoiOverlay getRoiOverlay() {
		for (MontageItemOverlay overlay : overlays) {
			if (overlay instanceof RoiOverlay) {
				return (RoiOverlay) overlay;
			}
		}

		return null;
	}

	static class MontagePanelItemBorder implements Border {

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, 0);
		}

		@Override
		public boolean isBorderOpaque() {
			return true;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(Color.BLACK);
			g.drawRect(x, y, width - 1, height - 1);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JCheckBoxMenuItem) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			
			if (item.getName().equals("roiItem")) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// TODO Add popup for ROI selection
					RoiManager roiManager = RoiManager.getInstance();
					if (roiManager == null) {
						roiManager = new RoiManager(false);
					}
					
					if (roiManager.getSelectedIndex() < 0) {
						// No ROI selected: show all per slice?
					} else {
						Roi[] selectedRois = roiManager.getSelectedRoisAsArray();
						RoiOverlay roiOverlay = getRoiOverlay();
						
						roiOverlay.setRois(selectedRois);
					}
					
					enableRoiDrawing();
					item.setState(true);
				} else {
					RoiOverlay roiOverlay = getRoiOverlay();
					roiOverlay.setRois(null);
					
					disableRoiDrawing();
					item.setState(false);
				}
			} else if (item.getName().equals("scalebarItem")) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					enableScalebarDrawing();
					item.setState(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					disableScalebarDrawing();
					item.setState(false);
				}
			}
			
			invalidate();
			repaint();
		}
	}
	
	/**
	 * TODO Document 0-based or 1-based
	 * 
	 * @param channel
	 * @return
	 */
	public MontageItemOverlay overlayForChannel(final int channel) {
		return overlays.get(channel);
	}

	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void disableScalebarDrawing() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			MontageItemOverlay item = iter.next();
			if (item instanceof ScalebarOverlay) {
				item.setDrawn(false);
			}
		}
	}

	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void enableScalebarDrawing() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			MontageItemOverlay item = iter.next();
			if (item instanceof ScalebarOverlay) {
				item.setDrawn(true);
			}
		}
	}

	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void enableRoiDrawing() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			MontageItemOverlay item = iter.next();
			if (item instanceof RoiOverlay) {
				item.setDrawn(true);
			}
		}
	}

	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void disableRoiDrawing() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			MontageItemOverlay item = iter.next();
			if (item instanceof RoiOverlay) {
				item.setDrawn(false);
			}
		}
	}

	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean hasRoi() {
		// FIXME Non-functional default implementation
		return overlays.contains(new RoiOverlay());
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean hasScalebar() {
		// FIXME Non-functional default implementation
		return overlays.contains(new ScalebarOverlay());
	}

	/**
	 * TODO Documentation
	 * @return
	 */
	public int getColumn() {
		Point upperLeftCorner = getLocation();
		return (int) (upperLeftCorner.getX() / getWidth());
	}

	/**
	 * TODO Documentation
	 * @return
	 */
	public int getRow() {
		Point upperLeftCorner = getLocation();
		return (int) (upperLeftCorner.getY() / getHeight());
	}
	
	/**
	 * @return the overlays
	 */
	public List<MontageItemOverlay> getOverlays() {
		return overlays;
	}

	/**
	 * @param overlays the overlays to set
	 */
	public void setOverlays(List<MontageItemOverlay> overlays) {
		this.overlays = overlays;
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return overlays.stream().noneMatch(o -> o.isDrawn());
	}

	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean hasDrawnOverlay() {
		return overlays.stream().anyMatch(o -> o.isDrawn());
	}

	/**
	 * @param menu the menu to set
	 */
	public void setMenu(MontageItemPopup menu) {
		this.menu = menu;
	}

}