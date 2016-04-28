// TODO Missing license header

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontageFrame extends JFrame implements ActionListener, ComponentListener {      
	
	MontagePanel panel;
	JButton compileButton;
	
	private Collection<ActionListener> actionListeners;
	
	public MontageFrame(MontageTool montageTool) {
		super();
		
		actionListeners = new LinkedList<>();
		
		panel = new MontagePanel(montageTool);
		getContentPane().add(BorderLayout.CENTER, panel);
		
		compileButton = new JButton("Compile montage");
		compileButton.addActionListener(this);
		getContentPane().add(BorderLayout.SOUTH, compileButton);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		setSize(200,200);
		setTitle("Montage");
		setVisible(true);
		
		panel.setSnapSize();
		
		addComponentListener(this);
	}
	
	/**
	 * TODO Documentation
	 * 
	 * @param actionListener
	 */
	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		actionListeners.forEach(l -> l.actionPerformed(event));
	}

	/**
	 * @return the panel
	 */
	public MontagePanel getPanel() {
		return panel;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		panel.setSnapSize();
	}

	@Override
	public void componentMoved(ComponentEvent e) { /* NB */	}

	@Override
	public void componentShown(ComponentEvent e) { /* NB */	}

	@Override
	public void componentHidden(ComponentEvent e) { /* NB */ }
	
}
