/**
 * 
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
	private boolean showStartPage;
	private IMachine machine;
	private boolean wasPaused;
	private PathSetupPage pathSetupPage;
	private SwtWindow window;
	private boolean romsChanged;
	private IPathChangeListener pathListener;


	public SetupWizard(IMachine machine, SwtWindow window, boolean showStartPage) {
		this.machine = machine;
		this.window = window;
		this.showStartPage = showStartPage;
		
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	@Override
	public IWizardPage getStartingPage() {
		if (!showStartPage) {
			return pathSetupPage;
		}
		return super.getStartingPage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		machine.getRomPathFileLocator().removeListener(pathListener);
		
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
