package v9t9.emulator.clients.builtin;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.DialogSettings;

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
		try {
			settings.save(getSettingsConfigurationPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setVideoRenderer(VideoRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}

	protected void clearConfigVar(String configVar) {
		DialogSettings settings = getApplicationSettings();
		settings.put(configVar, (String)null);
	}
	
	protected String selectFile(String title, String configVar, String defaultSubdir,
			String fileName, boolean isSave, boolean ifUndefined) {
		
		boolean isUndefined = false;
		DialogSettings settings = getApplicationSettings();
		String configPath = settings.get(configVar);
		if (configPath == null) {
			configPath = getBaseConfigurationPath() + defaultSubdir + File.separatorChar + fileName;
			isUndefined = true;
			File saveDir = new File(configPath);
			saveDir.getParentFile().mkdirs();
		} else if (ifUndefined) {
			return configPath;
		}
		
		String filename = openFileSelectionDialog(title, new File(configPath).getParent(), fileName, isSave);
		if (filename != null && isUndefined) {
			settings.put(configVar, filename);
		}
		return filename;
	}

	protected String selectDirectory(String title, String configVar, String defaultSubdir,
			boolean ifUndefined) {
		boolean isUndefined = false;
		DialogSettings settings = getApplicationSettings();
		String configDir = settings.get(configVar);
		if (configDir == null) {
			configDir = getBaseConfigurationPath() + File.separatorChar + defaultSubdir + File.separatorChar;
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
	abstract protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave);
	abstract protected String openDirectorySelectionDialog(String title, String directory);

	protected String getBaseConfigurationPath() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}

	protected DialogSettings getApplicationSettings() {
		if (settings == null) {
			settings = new DialogSettings("root");
			try {
				settings.load(getSettingsConfigurationPath());
			} catch (IOException e) {
			}
		}
		return settings;
	}

	private String getSettingsConfigurationPath() {
		return getBaseConfigurationPath() + "config";
	}
	
	protected void sendNMI() {
		machine.getCpu().setPin(Cpu.PIN_LOAD);
	}
	
	protected void sendReset() {
		machine.getCpu().setPin(Cpu.PIN_RESET);
	}
	protected void loadMachineState() {
		String filename = selectFile(
				"Select saved machine state", "MachineStatePath", "saves", 
				"save0.sav", false, false);
		
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
		boolean old = Machine.settingPauseMachine.getBoolean();
		Machine.settingPauseMachine.setBoolean(true);
		
		String filename = selectFile(
				"Select location to save machine state", "MachineStatePath", 
				"saves", "save0.sav", true, false);
		
		if (filename != null) {
			try {
				machine.saveState(filename);
			} catch (Throwable e1) {
				showErrorMessage("Save error", 
						"Failed to save machine state:\n\n" + e1.getMessage());
			}
		}
		Machine.settingPauseMachine.setBoolean(old);
		
	}

	protected void screenshot() {
		
		//String dirname = selectDirectory(
		//		"Select screenshot directory", "ScreenShotsPath", "screenshots", true);
		String filenameBase = selectFile(
				"Select screenshot file", "ScreenShotsBase", "screenshots", "screen.png", true, true);
		if (filenameBase != null) {
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
			if (saveFile.exists()) {
				showErrorMessage("Save error", 
						"Too many screenshots here!");
				clearConfigVar("ScreenShotsBase");
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
}