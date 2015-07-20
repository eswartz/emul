/*
  BaseEmulatorWindow.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
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

import org.apache.log4j.Logger;

import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.TerminatedException;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.gui.Emulator;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.settings.WorkspaceSettings;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.ISettingStorage;
import ejs.base.settings.SettingsSection;
import ejs.base.settings.XMLSettingStorage;
import ejs.base.utils.TextUtils;

public abstract class BaseEmulatorWindow {
	private static final Logger log = Logger.getLogger(BaseEmulatorWindow.class);
	
	/**
	 * 
	 */
	private static final String STATE = "state";
	/**
	 * 
	 */
	private static final String[] MACHINE_SAVE_FILE_EXTENSIONS = new String[] { "*.sav|V9t9 machine save file" };
	protected IVideoRenderer videoRenderer;
	protected final IMachine machine;

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
			InputStream fis = null;
			try {
				ISettingStorage storage = new XMLSettingStorage(STATE);
				fis = new BufferedInputStream(new FileInputStream(filename));
				ISettingSection settings = storage.load(fis);
				
				String modelId = settings.get("MachineModel");

				boolean changedMachines = false;
				EmulatorLocalServer server = null;
				
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
			        	
						server = new EmulatorLocalServer();
						IClient client = Emulator.create(server, modelId, clientId);
			        	
			        	loadState(client, server.getMachine(), settings);
			        	changedMachines = true;
			        }
				}
		        
				if (!changedMachines) {
					loadState(machine.getClient(), machine, settings);
				}
				else {
					Emulator.runServer(server);
				}

			} catch (Throwable e1) {
				log.error("Failed to load machine state", e1);
				machine.notifyEvent(Level.ERROR, 
						"Failed to load machine state:\n\n" + 
									(!TextUtils.isEmpty(e1.getMessage()) ? e1.getMessage() : e1.getClass()));
			
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

	/**
	 * @param client
	 * @param settings2 
	 * @param machine2 
	 */
	private static void loadState(IClient client, IMachine machine, ISettingSection settings) {
		String origWorkspace = settings.get(WorkspaceSettings.currentWorkspace.getName());
		if (origWorkspace != null) {
			try {
				WorkspaceSettings.loadFrom(
						Settings.getSettings(machine).getMachineSettings(), 
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
			Settings.getSettings(machine).getMachineSettings().load(workspace);
		}
		
		machine.loadState(settings);
		
		client.getVideoRenderer().getCanvasHandler().forceRedraw();		
	}

	abstract protected void showErrorMessage(String title, String msg);

	public void saveMachineState() {
		
		// get immediately
		ISettingSection settings = new SettingsSection(null);
		machine.saveState(settings);
		
		String filename = selectFile(
				"Select location to save machine state", 
				Settings.get(machine, settingMachineStatePath), 
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