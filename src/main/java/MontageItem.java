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
	
	/** TODO */
	private boolean drawOverlay = false;
	
	/** TODO */
	private boolean drawRois = false;
	
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
					menu = new MontageItemPopup((MontagePanel) getParent());
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
			if (overlayItem instanceof ChannelOverlay) {
				g.setColor(overlayItem.getColor());
				g.fillRect(overlayHeight,
						overlayElementCount * overlayHeight
								+ (overlayElementCount + 1) * overlayHeight,
						this.getWidth() - 2 * overlayHeight, overlayHeight);
				overlayElementCount++;
			}
			
			if (overlayItem instanceof RoiOverlay) {
				g.setColor(overlayItem.getColor());
				// TODO Improve computation of position
				g.drawString("R", 2, this.getHeight() - 2);
			}
			
			if (overlayItem instanceof ScalebarOverlay) {
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
				overlays.clear();
				menu.clearMenu();
			} else if (item.getName().equals("compositeItem")) {
				for (int i = 1; i <= tool.getImp().getNChannels(); i++) {
					MontageItemOverlay overlayForChannel = MontageUtil
							.getOverlayForChannel(tool.getImp(), i);
					overlays.add(overlayForChannel);
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
					overlays.add(new RoiOverlay(Color.WHITE));
					item.setState(true);
				} else {
					removeRoi();
					item.setState(false);
				}
			} else if (item.getName().equals("scalebarItem")) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					overlays.add(new ScalebarOverlay(Color.WHITE));
					item.setState(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					removeScalebar();
					item.setState(false);
				}
			} else {
				// TODO Handle exceptions / improve in general
				String[] splitName = item.getName().split("-");
				MontageItemOverlay overlayForChannel = MontageUtil
						.getOverlayForChannel(tool.getImp(),
								Integer.parseInt(splitName[1]));
				
				if (e.getStateChange() == ItemEvent.SELECTED) {
					overlays.add(overlayForChannel);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					// TODO Remove overlay
					overlays.remove(overlayForChannel);
				}
			}
			
			invalidate();
			repaint();
		}
	}
	
	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void removeScalebar() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			if (iter.next() instanceof ScalebarOverlay) {
				iter.remove();
			}
		}
	}
	
	/**
	 * TODO Documentation
	 * HACK Not optimized at all
	 */
	private void removeRoi() {
		Iterator<MontageItemOverlay> iter = overlays.iterator();
		while (iter.hasNext()) {
			if (iter.next() instanceof RoiOverlay) {
				iter.remove();
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
		return overlays.contains(new RoiOverlay(Color.WHITE));
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @return
	 */
	public boolean hasScalebar() {
		// FIXME Non-functional default implementation
		return overlays.contains(new ScalebarOverlay(Color.WHITE));
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
}