/*
  EmulatorServerBase.java

  (c) 2011-2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.tm.tcf.protocol.Protocol;

import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.machine.TerminatedException;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.DemoHandler;
import v9t9.engine.memory.GplMmio;
import v9t9.machine.f99b.machine.F99bMachineModel;
import v9t9.machine.ti99.machine.Enhanced48KForthTI994AMachineModel;
import v9t9.machine.ti99.machine.EnhancedTI994AMachineModel;
import v9t9.machine.ti99.machine.Forth9900MachineModel;
import v9t9.machine.ti99.machine.Forth9900StandaloneMachineModel;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;
import v9t9.machine.ti99.machine.StandardTI994MachineModel;
import v9t9.server.MachineModelFactory;
import v9t9.server.settings.SettingsHandler;
import v9t9.server.settings.WorkspaceSettings;
import v9t9.server.tcf.EmulatorTCFServer;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.ISettingStorage;
import ejs.base.settings.SettingsSection;
import ejs.base.settings.XMLSettingStorage;
import ejs.base.utils.TextUtils;

/**
 * @author ejs
 *
 */
public abstract class EmulatorServerBase {
	private static final Logger logger = Logger.getLogger(EmulatorServerBase.class);
	static {
		// FIXME workaround for ModuleSelector and mysterious error
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		MachineModelFactory.INSTANCE.register(
				StandardTI994AMachineModel.ID, StandardTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				StandardTI994MachineModel.ID, StandardTI994MachineModel.class);
		MachineModelFactory.INSTANCE.register(
				EnhancedTI994AMachineModel.ID, EnhancedTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				Enhanced48KForthTI994AMachineModel.ID, Enhanced48KForthTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				F99bMachineModel.ID, F99bMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				Forth9900MachineModel.ID, Forth9900MachineModel.class);
		MachineModelFactory.INSTANCE.register(
				Forth9900StandaloneMachineModel.ID, Forth9900StandaloneMachineModel.class);
	}
	
	private IMachine machine;
	private IClient client;
	private boolean inited;
	private ISettingsHandler settings;
	private IProperty bootRomsPath;
	private IProperty storedRamPath;
	private EmulatorTCFServer server;
	private boolean enableTCF;
	private Map<String, String> initSettings;
	private String loadFile;

    public EmulatorServerBase() {
    	settings = new SettingsHandler(WorkspaceSettings.currentWorkspace.getString()); 
		
    	settings.get(IVdpChip.settingDumpVdpAccess).setBoolean(true);
		settings.get(GplMmio.settingDumpGplAccess).setBoolean(true);
		
		bootRomsPath = settings.get(DataFiles.settingBootRomsPath);
		storedRamPath = settings.get(DataFiles.settingStoredRamPath);
    }
    
    /**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}
	protected void setupDefaults() {
		try {
			settings.getMachineSettings().setDirty(false);
			WorkspaceSettings.loadFrom(settings.getMachineSettings(),
					"workspace." + machine.getModel().getIdentifier());
		} catch (IOException e) {
		}
    	
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getErrorCount();
		
		machine.reload();
		
		if (client.getEventNotifier().getErrorCount() > barrier) {
			boolean anyReqdMissing = false;
			IPathFileLocator locator = machine.getRomPathFileLocator();
			for (MemoryEntryInfo info : machine.getMemoryModel().getRequiredRomMemoryEntries()) {
				URI uri = locator.findFile(machine.getSettings(), info);
				if (uri == null) {
					anyReqdMissing = true;
					break;
				}
			}
			if (anyReqdMissing) {
				settings.get(IClient.settingNewConfiguration).setValue(true);
			}
		}
	}
	
    public void init(String modelId) throws IOException {
    	if (bootRomsPath.getList().isEmpty())
    		bootRomsPath.getList().add(
    			settings.getUserSettings().getConfigDirectory() + "roms");
    	if (".".equals(storedRamPath.getString()))
    		storedRamPath.setString(
				settings.getUserSettings().getConfigDirectory() + "module_ram");
		DataFiles.addSearchPath(settings, storedRamPath.getString());
    	
		IMachineModel model = createModel(modelId);
		if (model == null)
			throw new FileNotFoundException("no model found: " + modelId);
        
        machine = model.createMachine(settings);
        

    	try {
    		WorkspaceSettings.loadFrom(settings.getMachineSettings(),
    				"workspace." + machine.getModel().getIdentifier());
    	} catch (IOException e) {
    		System.err.println("Setting up new configuration");
    	}
    	

    	try {
    		settings.getUserSettings().load();
    	} catch (IOException e) {
    		System.err.println("Setting up new instance");
    		settings.get(IClient.settingNewConfiguration).setBoolean(true);
    	}
    	
    	this.server = new EmulatorTCFServer(machine);
    	
    	// demo support
    	DemoHandler demoHandler = new DemoHandler(machine);
		machine.setDemoHandler(demoHandler);
    }

	abstract protected IMachineModel createModel(String modelId);

	public void setClient(IClient client) {
		this.client = client;
		machine.setClient(client);
		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void dispose() throws IOException {
		machine.getDemoHandler().dispose();
		machine.getMemory().save();
		
		settings.getMachineSettings().save();        	
    	settings.getUserSettings().save();   
	}
	

	private static final String STATE = "state";
	
	/**
	 * @param filename
	 */
	public void loadState(String filename) throws NotifyException {
		InputStream fis = null;
		try {
			ISettingStorage storage = new XMLSettingStorage(STATE);
			fis = new BufferedInputStream(new FileInputStream(filename));
			ISettingSection settings = storage.load(fis);
			
			String modelId = settings.get("MachineModel");

			boolean changedMachines = false;
//			EmulatorLocalServer server = null;
			
			if (modelId != null) {
		        if (!machine.getModel().getIdentifier().equals(modelId)) {
		        	throw new NotifyException(machine, "saved file machine is: " + modelId + " but this one is " + machine.getModel().getIdentifier());
//		        	String clientId = machine.getClient().getIdentifier();
//		        	try {
//		        		machine.getClient().close();
//		        	} catch (TerminatedException e) {
//		        	}
//		        	
//					server = new EmulatorLocalServer();
//					IClient newClient = Emulator.create(server, modelId, clientId);
//		        	
//		        	loadState(newClient, server.getMachine(), settings);
//		        	this.machine = server.getMachine();
//		        	this.client = newClient;
//		        	changedMachines = true;
		        }
			}
	        
			if (!changedMachines) {
				loadState(machine.getClient(), machine, settings);
			}
			else {
				runServer();
			}

		} catch (Throwable e1) {
			logger.error("Failed to load machine state", e1);
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
	

	/**
	 * @param client
	 * @param settings2 
	 * @param machine2 
	 */
	private void loadState(IClient client, IMachine machine, ISettingSection settings) {
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


	public void saveState(String filename) throws NotifyException {
		
		// get immediately
		ISettingSection settings = new SettingsSection(null);
		machine.saveState(settings);
		
		OutputStream fos = null;
		try {
			ISettingStorage storage = new XMLSettingStorage(STATE);
			fos = new BufferedOutputStream(new FileOutputStream(filename));
			storage.save(fos, settings);
		} catch (Throwable e1) {
			throw new NotifyException(machine, "Could not save machine state to '" + filename + "'", e1);
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

	public void run() {
		
		if (!inited) {
			inited = true;
	        setupDefaults();
	        loadState();
		}
		
		machine.reset();
        machine.start();
        
        //machine.getExecutor().getBreakpoints().addBreakpoint(new SimpleBreakpoint(0x1404, true));
        
        if (enableTCF)
        	server.run();
        
        if (initSettings != null) {
        	for (Map.Entry<String, String> ent : initSettings.entrySet()) {
        		IStoredSettings storage = settings.findSettingStorage(ent.getKey());
        		if (storage != null) {
        			storage.find(ent.getKey()).setValueFromString(ent.getValue());
        		}
        	}
        }
        
        if (loadFile != null) {
        	try {
				loadState(loadFile);
			} catch (NotifyException e) {
				machine.getEventNotifier().notifyEvent(e.getEvent());
			}
        }
        
        while (client.isAlive()) {
        	client.handleEvents();
	    	
	    	if (!machine.isAlive())
	    		break;
	    	
	    	// don't eat up CPU
	    	try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
        }
        
        Protocol.invokeLater(new Runnable() {
			public void run() {
				server.stop();
			}
        });
	}

	/**
	 * @return
	 */
	public ISettingsHandler getSettingsHandler() {
		return settings;
	}


	/**
	 * @return
	 */
	public MachineModelFactory getMachineModelFactory() {
		return MachineModelFactory.INSTANCE;
	}

	/**
	 * @param configdir
	 */
	public void setConfigDir(String configdir) {
		settings.getUserSettings().setConfigDirectory(configdir);
		settings.getMachineSettings().setConfigDirectory(configdir);
	}

	/**
	 * 
	 */
	public void enableTcf() {
		enableTCF = true;
	}

	/**
	 * @param settings
	 */
	public void setSettings(Map<String, String> settings) {
		this.initSettings = settings;
	}
	
	/**
	 * @param loadFile the loadFile to set
	 */
	public void setLoadFile(String loadFile) {
		this.loadFile = loadFile;
	}

	/**
	 * 
	 */
	public void runServer() {
		try {
			run();
		} catch (TerminatedException e) {
			// good
		} finally {
			try {
				dispose();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
