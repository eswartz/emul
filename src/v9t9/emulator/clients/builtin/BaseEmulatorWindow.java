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

	protected String selectFile(String configPath, String subdir, String fileName,
			int style) {
		
		DialogSettings settings = getApplicationSettings();
		String savePath = settings.get(configPath);
		if (savePath == null) {
			savePath = getBaseConfigurationPath() + File.separatorChar + subdir + File.separatorChar;
			File saveDir = new File(savePath);
			saveDir.mkdirs();
		}
		
		FileDialog dialog = new FileDialog(getShell(), style);
		dialog.setFilterPath(savePath);
		dialog.setFileName(fileName);
		String filename = dialog.open();
		
		return filename;
	}

	abstract protected Shell getShell();

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
		String filename = selectFile("MachineStatePath", "saves", "save0.sav", SWT.OPEN);
		
		if (filename != null) {
			try {
				machine.restoreState(filename);
			} catch (Throwable e1) {
				MessageDialog.openError(getShell(), "Load error", 
						"Failed to load machine state:\n\n" + e1.getMessage());
			
			}
		}
				
	}

	protected void saveMachineState() {
		String filename = selectFile("MachineStatePath", "saves", "save0.sav", SWT.SAVE);
		
		if (filename != null) {
			try {
				machine.saveState(filename);
			} catch (Throwable e1) {
				MessageDialog.openError(getShell(), "Save error", 
						"Failed to save machine state:\n\n" + e1.getMessage());
			}
		}		
	}

	protected void pasteClipboardToKeyboard() {
		Clipboard clip = new Clipboard(Display.getDefault());
		String contents = (String) clip.getContents(TextTransfer.getInstance());
		if (contents == null) {
			contents = (String) clip.getContents(RTFTransfer.getInstance());
		}
		if (contents != null) {
			machine.getClient().getKeyboardHandler().pasteText(contents);
		} else {
			
			
			MessageDialog.openError(getShell(), "Paste Error", 
					"Cannot paste: no text on clipboard");
		}
		clip.dispose();
		
	}


}