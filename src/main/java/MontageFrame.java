// TODO Missing license header

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;

import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontageFrame extends JFrame implements ActionListener {      
	
	MontagePanel panel;
	JButton compileButton;
	
	private Collection<ActionListener> actionListeners;
	
	public MontageFrame(LUT[] luts, MontageTool montageTool) {
		super();
		
		panel = new MontagePanel(luts, montageTool);
		getContentPane().add(BorderLayout.CENTER, panel);
		
		compileButton = new JButton("Compile montage");
		compileButton.addActionListener(this);
		getContentPane().add(BorderLayout.SOUTH, compileButton);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		setSize(200,200);
		setTitle("Montage");
		setVisible(true);
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
	
}
