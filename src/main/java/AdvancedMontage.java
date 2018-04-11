/*-
 * #%L
 * Advanced Montage.
 * %%
 * Copyright (C) 2016 - 2018 Board of Regents of the University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
@Plugin(type = Command.class, menuPath = "BIC>Montage")
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

}
