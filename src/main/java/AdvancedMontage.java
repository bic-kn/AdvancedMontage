// TODO Missing license header

import org.scijava.command.Command;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.ui.UIService;

import java.util.LinkedList;
import java.util.List;

import net.imagej.Dataset;
import net.imagej.ImageJ;

import ij.ImagePlus;
import ij.WindowManager;

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
	
	@Parameter
	private PrefService prefService;
	
	@Parameter
	private UIService uiService;
	
	/** TODO Documentation */
	private MontageFrame montageFrame;
	
	/** TODO Documentation */
	private MontageTool tool;
	
	@Override
	public void run() {
		if (tool == null) {
			tool = new MontageTool(prefService);
		}
		tool.setImp(dataset);

		List<ImagePlus> imps = new LinkedList<>();
		for (int id : WindowManager.getIDList()) {
			imps.add(WindowManager.getImage(id));
		}
		
		int firstWidth = -1;
		int firstHeight = -1;
		for (ImagePlus imp : imps) {
			if (firstWidth < 0 && firstHeight < 0) {
				firstWidth = imp.getWidth();
				firstHeight = imp.getHeight();
				continue;
			}
			
			if (imp.getWidth() != firstWidth || imp.getHeight() != firstHeight) {
				imps.clear();
				imps.add(dataset);
				
				uiService.showDialog("Open images differ in size. Continuing with the one in focus.");
			}
		}
		tool.setImps(imps);

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
		
		final Dataset dataset1 = (Dataset) ij.io().open("/home/stefan/Dropbox/Konstanz/Chovancova/cell_seg_max_proj.tif");
		final Dataset dataset2 = (Dataset) ij.io().open("/home/stefan/Pictures/hela-cells.tif");
		ij.ui().show(dataset1);
		ij.ui().show(dataset2);
		
		ij.command().run(AdvancedMontage.class, true);
	}
	
}
