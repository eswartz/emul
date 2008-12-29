package v9t9.emulator.clients.builtin;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.runtime.Cpu;

public abstract class BaseEmulatorWindow {

	protected VideoRenderer videoRenderer;
	protected final Machine machine;
	protected DialogSettings settings;

	public BaseEmulatorWindow(Machine machine) {
		this.machine = machine;
	}
	
	public void dispose() {
		
	}
	public void setVideoRenderer(VideoRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}

	protected String selectFile(String title, String configPath, String subdir,
			String fileName, boolean isSave) {
		
		DialogSettings settings = getApplicationSettings();
		String savePath = settings.get(configPath);
		if (savePath == null) {
			savePath = getBaseConfigurationPath() + File.separatorChar + subdir + File.separatorChar;
			File saveDir = new File(savePath);
			saveDir.mkdirs();
		}
		
		String filename = openFileSelectionDialog(title, savePath, fileName, isSave);
		
		return filename;
	}

	abstract protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave);

	protected String getBaseConfigurationPath() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}

	protected DialogSettings getApplicationSettings() {
		if (settings == null) {
			settings = new DialogSettings("root");
			try {
				settings.load(getBaseConfigurationPath() + "config");
			} catch (IOException e) {
			}
		}
		return settings;
	}
	
	protected void sendNMI() {
		machine.getCpu().setPin(Cpu.PIN_LOAD);
	}
	
	protected void sendReset() {
		machine.getCpu().setPin(Cpu.PIN_RESET);
	}
	protected void loadMachineState() {
		String filename = selectFile("Select saved machine state", "MachineStatePath", "saves", "save0.sav", false);
		
		if (filename != null) {
			try {
				machine.restoreState(filename);
			} catch (Throwable e1) {
				showErrorMessage("Load error", 
						"Failed to load machine state:\n\n" + e1.getMessage());
			
			}
		}
	}

	abstract protected void showErrorMessage(String title, String msg);

	protected void saveMachineState() {
		String filename = selectFile("Select location to save machine state", "MachineStatePath", "saves", "save0.sav", true);
		
		if (filename != null) {
			try {
				machine.saveState(filename);
			} catch (Throwable e1) {
				showErrorMessage("Save error", 
						"Failed to save machine state:\n\n" + e1.getMessage());
			}
		}	
	}

}