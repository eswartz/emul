package v9t9.emulator.clients.builtin;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.DialogSettings;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.runtime.Cpu;

public abstract class BaseEmulatorWindow {

	/**
	 * 
	 */
	private static final String[] MACHINE_SAVE_FILE_EXTENSIONS = new String[] { ".sav|V9t9 machine save file" };
	protected VideoRenderer videoRenderer;
	protected final Machine machine;

	public BaseEmulatorWindow(Machine machine) {
		this.machine = machine;
		EmulatorSettings.getInstance().load();
	}
	
	public void dispose() {
		EmulatorSettings.getInstance().save();
	}
	
	public void setVideoRenderer(VideoRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}

	protected String selectFile(String title, String configVar, String defaultSubdir,
			String fileName, boolean isSave, boolean ifUndefined, String[] extensions) {
		
		boolean isUndefined = false;
		DialogSettings settings = EmulatorSettings.getInstance().getApplicationSettings();
		String configPath = settings.get(configVar);
		if (configPath == null) {
			configPath = EmulatorSettings.getInstance().getBaseConfigurationPath() + defaultSubdir + File.separatorChar + fileName;
			isUndefined = true;
			File saveDir = new File(configPath);
			saveDir.getParentFile().mkdirs();
		} else if (ifUndefined) {
			return configPath;
		}
		
		File dir = new File(configPath);
		if (!dir.isDirectory())
			dir = dir.getParentFile();
		
		String filename = openFileSelectionDialog(title, dir.getAbsolutePath(), fileName, 
				isSave, extensions);
		if (filename != null && isUndefined) {
			settings.put(configVar, filename);
		}
		return filename;
	}

	protected String selectDirectory(String title, String configVar, String defaultSubdir,
			boolean ifUndefined) {
		boolean isUndefined = false;
		DialogSettings settings = EmulatorSettings.getInstance().getApplicationSettings();
		String configDir = settings.get(configVar);
		if (configDir == null) {
			configDir = EmulatorSettings.getInstance().getBaseConfigurationPath() + File.separatorChar + defaultSubdir + File.separatorChar;
			File saveDir = new File(configDir);
			saveDir.mkdirs();
			isUndefined = true;
		} else if (ifUndefined) {
			return configDir;
		}
		
		String dirname = openDirectorySelectionDialog(title, configDir);
		if (dirname != null && isUndefined) {
			settings.put(configVar, dirname);
		}
		return dirname;
	}
	/**
	 * Open a file selection dialog (which is used to open or save files).
	 * @param title title of dialog
	 * @param directory base directory, or <code>null</code>
	 * @param fileName base filename
	 * @param isSave true: saving a file, false: opening
	 * @param extensions array of entries in the form "extension|label"; first entry is default extension
	 * @return full path to selected file, or <code>null</code> if canceled
	 */
	abstract protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave, String[] extensions);
	abstract protected String openDirectorySelectionDialog(String title, String directory);

	protected void sendNMI() {
		machine.getCpu().setPin(Cpu.PIN_LOAD);
	}
	
	protected void sendReset() {
		machine.getCpu().setPin(Cpu.PIN_RESET);
	}
	protected void loadMachineState() {
		String filename = selectFile(
				"Select saved machine state", "MachineStatePath", "saves", 
				"save0.sav", false, false, MACHINE_SAVE_FILE_EXTENSIONS);
		
		if (filename != null) {
			try {
				DialogSettings settings = new DialogSettings("state");
				settings.load(filename);
				
				machine.restoreState(settings);
			} catch (Throwable e1) {
				showErrorMessage("Load error", 
						"Failed to load machine state:\n\n" + e1.getMessage());
			
			}
		}
	}

	abstract protected void showErrorMessage(String title, String msg);

	protected void saveMachineState() {
		
		// get immediately
		DialogSettings settings = new DialogSettings("state");
		machine.saveState(settings);
		
		String filename = selectFile(
				"Select location to save machine state", "MachineStatePath", 
				"saves", "save0.sav", true, false, MACHINE_SAVE_FILE_EXTENSIONS);
		
		if (filename != null) {
			try {
				settings.save(filename);
			} catch (Throwable e1) {
				showErrorMessage("Save error", 
						"Failed to save machine state:\n\n" + e1.getMessage());
			}
		}
	}

	protected void screenshot() {
		
		//String dirname = selectDirectory(
		//		"Select screenshot directory", "ScreenShotsPath", "screenshots", true);
		String filenameBase = selectFile(
				"Select screenshot file", "ScreenShotsBase", "screenshots", "screen.png", true, true, 
				new String[] { ".png|PNG file" });
		if (filenameBase != null) {
			File saveFile = getUniqueFile(filenameBase);
			if (saveFile == null) {
				showErrorMessage("Save error", 
						"Too many screenshots here!");
				EmulatorSettings.getInstance().clearConfigVar("ScreenShotsBase");
				screenshot();
			} else {
				try {
					videoRenderer.saveScreenShot(saveFile);
				} catch (IOException e) {
					showErrorMessage("Save error", 
						"Failed to write file:\n\n" + e.getMessage());
				}
			}
		}
	}
	
	protected File getUniqueFile(String filenameBase) {
		File fileBase = new File(filenameBase);
		File dir = fileBase.getParentFile();
		String base = fileBase.getName();
		int extPtr = base.lastIndexOf('.');
		if (extPtr < 0) extPtr = base.length();
		String ext = base.substring(extPtr);
		base = base.substring(0, extPtr);
		
		File saveFile = null; 
		for (int count = 0; count < 10000; count++) {
			saveFile = new File(dir, base + (count != 0 ? "" + count : "") + ext);
			if (!saveFile.exists())
				break;
		}
		if (saveFile.exists())
			return null;
		return saveFile;
	}
}