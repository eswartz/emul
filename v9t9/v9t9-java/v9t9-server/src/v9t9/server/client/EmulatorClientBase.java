/**
 * 
 */
package v9t9.server.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.tm.tcf.protocol.Protocol;

import ejs.base.properties.IProperty;

import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModule;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.Settings;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.modules.ModuleLoader;
import v9t9.engine.modules.ModuleManager;
import v9t9.server.settings.SettingsHandler;
import v9t9.server.settings.WorkspaceSettings;
import v9t9.server.tcf.EmulatorTCFServer;

/**
 * @author ejs
 *
 */
public abstract class EmulatorClientBase {
	
	private IMemory memory;
	private IMachine machine;
	private IMemoryModel memoryModel;
	private IClient client;
	private boolean inited;
	private ISettingsHandler settings;
	private IProperty bootRomsPath;
	private IProperty storedRamPath;
	private EmulatorTCFServer server;

    public EmulatorClientBase() {
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
			settings.getWorkspaceSettings().setDirty(false);
			WorkspaceSettings.loadFrom(settings.getWorkspaceSettings(),
					"workspace." + machine.getModel().getIdentifier());
		} catch (IOException e) {
		}
    	
    }
    
	protected void registerModules(URL url) {
		if (url == null)
			return;
		
    	ISettingsHandler settings = Settings.getSettings(machine);
		//boolean anyErrors = false;
		InputStream is = null;
		try {
			is = url.openStream();
			List<IModule> modList = ModuleLoader.loadModuleList(settings, is);
			machine.getModuleManager().addModules(modList);
		} catch (NotifyException e) {
			machine.getClient().getEventNotifier().notifyEvent(e.getEvent());
			//anyErrors = true;
		} catch (IOException e) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Could not load module list: " + e.getMessage());

		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { }
			}
		}
		
		/*
		if (anyErrors) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Be sure your " + DataFiles.settingBootRomsPath.getName() + " setting is established in "
					+ settings.findSettingStorage(DataFiles.settingBootRomsPath.getName()).getConfigFilePath());
		}
		*/
	}
	
	protected void loadState() {
		IProperty lastLoadedModule = settings.get(ModuleManager.settingLastLoadedModule);
		IProperty moduleList = settings.get(IMachine.settingModuleList);
		
		int barrier = client.getEventNotifier().getErrorCount();
		memoryModel.loadMemory(client.getEventNotifier());
		
		if (machine.getModuleManager() != null) {
			// first, get stock module database
			registerModules(machine.getModuleManager().getStockDatabaseURL());
			
			// then load any user entries
			String dbNameList = moduleList.getString();
    		if (dbNameList.length() > 0) {
    			String[] dbNames = dbNameList.split(";");
    			for (String dbName : dbNames) {
    				File file = DataFiles.resolveFile(settings, dbName);
    				if (file != null && file.exists()) {
						try {
							registerModules(file.toURI().toURL());
						} catch (MalformedURLException e) {
							machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
									"Could not resolve module list from " + moduleList.getName() + ": " + e.getMessage());
						}
					}
						
    			}
    		}
    		
    		// reset state
    		try {
				if (lastLoadedModule.getString().length() > 0)
	        		machine.getModuleManager().switchModule(
	        				lastLoadedModule.getString());
    		} catch (NotifyException e) {
				machine.notifyEvent(e.getEvent());
			}
		}
		
		if (client.getEventNotifier().getErrorCount() > barrier) {
			IStoredSettings storedSettings = settings.findSettingStorage(bootRomsPath.getName()); 
			machine.notifyEvent(IEventNotifier.Level.ERROR,
					"Failed to load startup ROMs; please edit your " + bootRomsPath.getName() + " in '"
					+ storedSettings.getConfigFilePath() + "'");
			//EmulatorSettings.INSTANCE.save();
		}
	}
	
    public void init(String modelId) throws IOException {
    	

		//WorkspaceSettings.CURRENT.register(ModuleManager.settingLastLoadedModule);

    	//settingsHandler.getInstanceSettings().findOrCreate(
    	if (bootRomsPath.getList().isEmpty())
    		bootRomsPath.getList().add(
    			settings.getInstanceSettings().getConfigDirectory() + "roms");
    	//settingsHandler.getInstanceSettings().findOrCreate(
    	if (".".equals(storedRamPath.getString()))
    		storedRamPath.setString(
				settings.getInstanceSettings().getConfigDirectory() + "module_ram");
		DataFiles.addSearchPath(settings, storedRamPath.getString());
    	
		IMachineModel model = createModel(modelId);
        
        machine = model.createMachine(settings);
        

    	try {
    		WorkspaceSettings.loadFrom(settings.getWorkspaceSettings(),
    				WorkspaceSettings.currentWorkspace.getString());
    				
    	} catch (IOException e) {
    		System.err.println("Setting up new configuration");
    	}
    	

    	try {
    		settings.getInstanceSettings().load();
    	} catch (IOException e) {
    		System.err.println("Setting up new instance");
    	}
    	
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();

    	this.server = new EmulatorTCFServer(machine);
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
		machine.getMemory().save();
		
		settings.getWorkspaceSettings().save();        	
    	settings.getInstanceSettings().save();   
	}

	public void run() {
		
		if (!inited) {
			inited = true;
	        setupDefaults();
	        loadState();
		}
		
        machine.start();
        machine.reset();
        
        server.run();
        
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


}
