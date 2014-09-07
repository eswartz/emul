/*
  SetupWizard.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import v9t9.common.files.IPathFileLocator.IPathChangeListener;
import v9t9.common.machine.IMachine;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.SwtWindow;

/**
 * @author ejs
 *
 */
public class SetupWizard extends Wizard {
	public enum Page {
		INTRO,
		PATHS,
		MODULES
	}
	
	private Page startPage;
	private IMachine machine;
	private boolean wasPaused;
	private PathSetupPage pathSetupPage;
	private SwtWindow window;
	private boolean romsChanged;
	private IPathChangeListener pathListener;
	private ModuleListPage moduleListPage;


	public SetupWizard(IMachine machine, SwtWindow window, Page startPage) {
		this.machine = machine;
		this.window = window;
		this.startPage = startPage;
		
		wasPaused = machine.setPaused(true);
		
		pathListener = new IPathChangeListener() {
			
			@Override
			public void pathsChanged() {
				romsChanged = true;
			}
		};
		machine.getRomPathFileLocator().addListener(pathListener);

		setWindowTitle("Setup V9t9");
		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(
				EmulatorGuiData.getDataURL("icons/v9t9_64.png")));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new SetupIntroPage(machine));
		pathSetupPage = new PathSetupPage(machine, window); 
		addPage(pathSetupPage);
		if (machine.getModuleManager() != null) {
			moduleListPage = new ModuleListPage(machine, window); 
			addPage(moduleListPage);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	@Override
	public IWizardPage getStartingPage() {
		if (startPage == Page.PATHS) {
			return pathSetupPage;
		}
		if (startPage == Page.MODULES && moduleListPage != null) {
			return moduleListPage;
		}
		return super.getStartingPage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		machine.getRomPathFileLocator().removeListener(pathListener);
		
		if (moduleListPage != null && !moduleListPage.save())
			return false;
		
		
		if (romsChanged) {
			machine.getMemoryModel().loadMemory(machine.getEventNotifier());
			machine.reset();
			machine.setPaused(false);
		}
		else {
			machine.setPaused(wasPaused);
		}
		
		return true;
	}
}
