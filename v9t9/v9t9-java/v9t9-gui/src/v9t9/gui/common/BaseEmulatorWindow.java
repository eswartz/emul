/*
  BaseEmulatorWindow.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.common;


import java.io.File;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.server.client.EmulatorServerBase;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

public abstract class BaseEmulatorWindow {
	/**
	 * 
	 */
	private static final String[] MACHINE_SAVE_FILE_EXTENSIONS = new String[] { "*.sav|V9t9 machine save file" };
	protected IVideoRenderer videoRenderer;
	protected final IMachine machine;
	private final EmulatorServerBase server;
	
	private String lastLoadedState;
	static public final SettingSchema settingMonitorDrawing = new SettingSchema(
			ISettingsHandler.MACHINE,
			"MonitorDrawing", Boolean.TRUE);
	static public final SettingSchema settingMonitorEffect = new SettingSchema(
			ISettingsHandler.MACHINE,
			"MonitorEffect", "");
	static public final SettingSchema settingZoomLevel = new SettingSchema(
			ISettingsHandler.MACHINE,
			"ZoomLevel", new Integer(3));
	static public final SettingSchema settingFullScreen = new SettingSchema(
			ISettingsHandler.MACHINE,
			"FullScreen", Boolean.FALSE);

	static public final SettingSchema settingShowRnDBar = new SettingSchema(
			ISettingsHandler.MACHINE,
			"ShowRnDBar", Boolean.FALSE);
	static public final SettingSchema settingScreenshotPlain = new SettingSchema(
			ISettingsHandler.MACHINE,
			"SavePlainScreenshot", Boolean.FALSE);

	// not persisted
	static public final SettingSchema settingMachineStatePath = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"MachineStatePath", "");
	static public final SettingSchema settingScreenShotsBase = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"ScreenShotsBase", "");
	
	public BaseEmulatorWindow(EmulatorServerBase server, IMachine machine) {
		this.server = server;
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

	protected String selectFile(String title, IProperty configVar, String defaultSubdir,
			String fileName, boolean isSave, boolean ifUndefined, String[] extensions) {
		
		IStoredSettings workspace = machine.getSettings().getMachineSettings();
		ISettingSection settings = workspace.getSettings();
		configVar.loadState(settings);
		String configPath = configVar.getString();
		if (configPath == null || configPath.length() == 0) {
			configPath = workspace.getConfigDirectory() + defaultSubdir + File.separatorChar + fileName;
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
		if (filename != null) {
			configVar.setString(new File(filename).getParent());
		}
		return filename;
	}

	protected String selectDirectory(String title, IProperty configVar, String defaultSubdir,
			boolean ifUndefined) {
		boolean isUndefined = false;
		IStoredSettings workspace = machine.getSettings().getMachineSettings();
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
				"Select saved machine state", 
				Settings.get(machine, settingMachineStatePath),
				"saves",
				lastLoadedState,
				false, false, MACHINE_SAVE_FILE_EXTENSIONS);
		
		if (filename != null) {
			lastLoadedState = filename;
			try {
				server.loadState(filename);
			} catch (NotifyException e) {
				showErrorMessage("Load Error", e.getMessage());
			}
		}
	}

	abstract protected void showErrorMessage(String title, String msg);

	public void saveMachineState() {

		boolean wasPaused = machine.isPaused();
		machine.setPaused(true);
		
		String filename = selectFile(
				"Select location to save machine state", 
				Settings.get(machine, settingMachineStatePath), 
				"saves", "save0.sav", true, false, MACHINE_SAVE_FILE_EXTENSIONS);
	
		if (filename != null) {
			try {
				server.saveState(filename);
			} catch (NotifyException e) {
				showErrorMessage("Save Error", e.getMessage());
			}
		}
		
		machine.setPaused(wasPaused);
	}
	
	public void screenshot() {
		boolean plain = machine.getSettings().get(BaseEmulatorWindow.settingScreenshotPlain).getBoolean();

		IProperty screenShotsBase = machine.getSettings().get(BaseEmulatorWindow.settingScreenShotsBase);
		String filenameBase = selectFile(
				"Select screenshot file", 
				screenShotsBase, 
				"screenshots", 
				"screen.png", 
				true, true, 
				new String[] { "*.png|PNG file" }
				);
		
		if (filenameBase != null) {
			File saveFile = getUniqueFile(filenameBase);
			if (saveFile == null) {
				machine.notifyEvent(Level.ERROR, 
						"Too many screenshots here!");
				screenShotsBase.setString("");
				screenshot();
			} else {
				try {
					videoRenderer.saveScreenShot(saveFile, plain);
					
					machine.getEventNotifier().notifyEvent(
							null, Level.INFO, "Recorded screenshot to " + saveFile);
				} catch (Throwable e) {
					showErrorMessage("Save error", 
						"Failed to write file:\n\n" + e.getMessage());
				}
			}
		}
	}
	
	public static File getUniqueFile(String filenameBase) {
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