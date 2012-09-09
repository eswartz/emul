package v9t9.gui.common;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.ISettingStorage;
import ejs.base.settings.SettingsSection;
import ejs.base.settings.XMLSettingStorage;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.TerminatedException;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.gui.Emulator;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.settings.WorkspaceSettings;

public abstract class BaseEmulatorWindow {

	/**
	 * 
	 */
	private static final String STATE = "state";
	/**
	 * 
	 */
	private static final String[] MACHINE_SAVE_FILE_EXTENSIONS = new String[] { ".sav|V9t9 machine save file" };
	protected IVideoRenderer videoRenderer;
	protected final IMachine machine;
	static public final SettingSchema settingMonitorDrawing = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"MonitorDrawing", new Boolean(true));
	static public final SettingSchema settingZoomLevel = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"ZoomLevel", new Integer(3));
	static public final SettingSchema settingFullScreen = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"FullScreen", new Boolean(false));

	static public final SettingSchema settingShowRnDBar = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"ShowRnDBar", new Boolean(false));

	// not persisted
	static public final SettingSchema settingMachineStatePath = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"MachineStatePath", "");
	static public final SettingSchema settingScreenShotsBase = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"ScreenShotsBase", "");
	
	public BaseEmulatorWindow(IMachine machine) {
		this.machine = machine;
		//EmulatorSettings.INSTANCE.load();
	}
	
	public void dispose() {
		videoRenderer.dispose();
		//EmulatorSettings.INSTANCE.save();
	}
	
	public void setVideoRenderer(IVideoRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}
	
	/**
	 * @return the videoRenderer
	 */
	public IVideoRenderer getVideoRenderer() {
		return videoRenderer;
	}

	protected String selectFile(String title, SettingSchema configVarSchema, String defaultSubdir,
			String fileName, boolean isSave, boolean ifUndefined, String[] extensions) {
		
		IProperty configVar = Settings.get(machine, configVarSchema);
		
		boolean isUndefined = false;
		IStoredSettings workspace = machine.getSettings().getWorkspaceSettings();
		ISettingSection settings = workspace.getSettings();
		configVar.loadState(settings);
		String configPath = configVar.getString();
		if (configPath == null || configPath.length() == 0) {
			configPath = workspace.getConfigDirectory() + defaultSubdir + File.separatorChar + fileName;
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
			configVar.setString(filename);
		}
		return filename;
	}

	protected String selectDirectory(String title, IProperty configVar, String defaultSubdir,
			boolean ifUndefined) {
		boolean isUndefined = false;
		IStoredSettings workspace = machine.getSettings().getWorkspaceSettings();
		ISettingSection settings = workspace.getSettings();
		configVar.loadState(settings);
		String configDir = configVar.getString();
		if (configDir == null || configDir.length() == 0) {
			configDir = workspace.getConfigDirectory() + File.separatorChar + defaultSubdir + File.separatorChar;
			File saveDir = new File(configDir);
			saveDir.mkdirs();
			isUndefined = true;
		} else if (ifUndefined) {
			return configDir;
		}
		
		String dirname = openDirectorySelectionDialog(title, configDir);
		if (dirname != null && isUndefined) {
			configVar.setString(dirname);
			configVar.saveState(settings);
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

	public void loadMachineState() {
		String filename = selectFile(
				"Select saved machine state", settingMachineStatePath, "saves", 
				null, false, false, MACHINE_SAVE_FILE_EXTENSIONS);
		
		if (filename != null) {
			InputStream fis = null;
			try {
				ISettingStorage storage = new XMLSettingStorage(STATE);
				fis = new BufferedInputStream(new FileInputStream(filename));
				ISettingSection settings = storage.load(fis);
				
				String modelId = settings.get("MachineModel");

				if (modelId != null) {
			        if (!machine.getModel().getIdentifier().equals(modelId)) {
			        	String clientId = machine.getClient().getIdentifier();
			        	try {
			        		machine.getClient().close();
			        	} catch (TerminatedException e) {
			        	}
			        	try {
			        		dispose();
			        	} catch (Throwable t) {
			        		t.printStackTrace();
			        	}
			        	
			        	Emulator.createAndRun(new EmulatorLocalServer(), modelId, clientId);

			        	// $NOTREACHED$
			        	return;
			        }
				}
		        
				String origWorkspace = settings.get(WorkspaceSettings.currentWorkspace.getName());
				if (origWorkspace != null) {
					try {
						WorkspaceSettings.loadFrom(
								Settings.getSettings(machine).getWorkspaceSettings(), 
								origWorkspace);
					} catch (IOException e) {
						machine.notifyEvent(
								Level.WARNING, 
								MessageFormat.format(
										"Could not find the workspace ''{0}'' referenced in the saved state",
										origWorkspace));
					}
				}
				
				ISettingSection workspace = settings.getSection("Workspace");
				if (workspace != null) {
					Settings.getSettings(machine).getWorkspaceSettings().load(workspace);
				}
				
				boolean wasPaused = machine.setPaused(true);
				machine.loadState(settings);
				machine.setPaused(wasPaused);
				
				videoRenderer.getCanvasHandler().forceRedraw();
			} catch (Throwable e1) {
				machine.notifyEvent(Level.ERROR, 
						"Failed to load machine state:\n\n" + e1.getMessage());
			
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

	abstract protected void showErrorMessage(String title, String msg);

	public void saveMachineState() {
		
		// get immediately
		ISettingSection settings = new SettingsSection(null);
		machine.saveState(settings);
		
		String filename = selectFile(
				"Select location to save machine state", settingMachineStatePath, 
				"saves", "save0.sav", true, false, MACHINE_SAVE_FILE_EXTENSIONS);
		
		if (filename != null) {
			OutputStream fos = null;
			try {
				ISettingStorage storage = new XMLSettingStorage(STATE);
				fos = new BufferedOutputStream(new FileOutputStream(filename));
				storage.save(fos, settings);
			} catch (Throwable e1) {
				showErrorMessage("Save error", 
						"Failed to save machine state:\n\n" + e1.getMessage());
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

	public File screenshot() {
		
		String filenameBase = selectFile(
				"Select screenshot file", settingScreenShotsBase, "screenshots", "screen.png", true, true, 
				new String[] { ".png|PNG file" });
		if (filenameBase != null) {
			File saveFile = getUniqueFile(filenameBase);
			if (saveFile == null) {
				machine.notifyEvent(Level.ERROR, 
						"Too many screenshots here!");
				machine.getSettings().getInstanceSettings().clearConfigVar("ScreenShotsBase");
				return screenshot();
			} else {
				try {
					videoRenderer.saveScreenShot(saveFile);
					return saveFile;
				} catch (IOException e) {
					showErrorMessage("Save error", 
						"Failed to write file:\n\n" + e.getMessage());
					return null;
				}
			}
		}
		return null;
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