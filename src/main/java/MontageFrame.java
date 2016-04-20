// TODO Missing license header

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ij.ImagePlus;
import ij.process.LUT;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MontageFrame extends JFrame implements ActionListener {      
	
	JFrame frame;
	JPanel DrawPanel;
	JButton button;
	
	private MontagePanel panel;
	private MontageCompiler compiler;
	private MontageTool montageTool;
	
	public MontageFrame(LUT[] luts, MontageTool montageTool) {
		frame = new JFrame();
		button = new JButton("Compile montage");
		button.addActionListener(this);
		
		panel = new MontagePanel(luts);
		
		frame.getContentPane().add(BorderLayout.SOUTH, button);
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		frame.setSize(200,200);
		frame.setTitle("Montage");
		frame.setVisible(true);
		
		compiler = new MontageCompiler(montageTool.getImp());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		// TODO New thread for montage creation
		ImagePlus montage = compiler.compileMontage(panel);
		
		montage.show();
	}
	
}
