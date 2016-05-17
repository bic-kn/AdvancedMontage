// TODO Missing license header

import org.scijava.command.Command;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImageJ;

import ij.ImagePlus;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Command.class, menuPath = "Plugins>BIC>Montage")
public class AdvancedMontage implements Command {

	@Parameter
	private ImagePlus dataset;
	
	@Parameter
    private ConvertService convertService;
	
	/** TODO Documentation */
	private MontageFrame montageFrame;
	
	/** TODO Documentation */
	private MontageTool tool;
	
	@Override
	public void run() {
		if (tool == null) {
			tool = new MontageTool();
		}
		tool.setImp(dataset);

		MontageCompiler compiler = new MontageCompiler(tool);
		montageFrame = new MontageFrame(tool);
		montageFrame.addActionListener(compiler);
		tool.setFrame(montageFrame);

		tool.run("");
	}

	/** Tests our command. */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);
		
		final Dataset datasetFromService = ij.dataset().open("/home/stefan/Dropbox/Konstanz/Chovancova/cell_seg_max_proj.tif");
		ij.ui().show(datasetFromService);
		
		ij.command().run(AdvancedMontage.class, true);
	}
	
}
