// TODO Missing license header

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.Border;

import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
class MontageItem extends JButton implements ActionListener, ItemListener {

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
		
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Show popup menu
				// MontageItemPopup menu = new MontageItemPopup();
				// menu.show(e.getComponent(), e.getX(), e.getY());

				// Somehow get available channels

				// Add composite
			}

		});

		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

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
				if (menu == null) {
					menu = new MontageItemPopup(tool);
					add(menu);
					menu.init();
				}
				
				menu.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof MenuItem) {
			MenuItem item = (MenuItem) e.getSource();
			if (item.getName().equals("clearItem")) {
				overlays.forEach(overlay -> overlay.setDrawn(false));
				menu.clearMenu();
			} else if (item.getName().equals("compositeItem")) {
				for (int i = 0; i < tool.getImp().getNChannels(); i++) {
					overlays.get(i).setDrawn(true);
				}
				menu.composite();
			}
		}
		
		invalidate();
		repaint();
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
		if (e.getSource() instanceof CheckboxMenuItem) {
			CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();
			
			if (item.getName().equals("roiItem")) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					enableRoiDrawing();
					item.setState(true);
				} else {
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
			} else {
				// TODO Handle exceptions / improve in general
				String[] splitName = item.getName().split("-");
				MontageItemOverlay overlay = overlayForChannel(Integer.parseInt(splitName[1])-1);
				
				if (e.getStateChange() == ItemEvent.SELECTED) {					
					overlay.setDrawn(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					overlay.setDrawn(false);
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
	
	public boolean isEmpty() {
		return overlays.isEmpty();
	}

	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean hasDrawnOverlay() {
		return overlays.stream().anyMatch(o -> o.isDrawn());
	}

}