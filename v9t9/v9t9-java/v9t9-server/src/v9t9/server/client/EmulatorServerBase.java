/**
 * 
 */
package v9t9.server.client;

import java.io.IOException;

import org.eclipse.tm.tcf.protocol.Protocol;

import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.DataFiles;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.settings.IStoredSettings;
import v9t9.engine.memory.GplMmio;
import v9t9.machine.f99b.machine.F99bMachineModel;
import v9t9.machine.ti99.machine.Enhanced48KForthTI994AMachineModel;
import v9t9.machine.ti99.machine.EnhancedTI994AMachineModel;
import v9t9.machine.ti99.machine.StandardMachineModel;
import v9t9.server.MachineModelFactory;
import v9t9.server.settings.SettingsHandler;
import v9t9.server.settings.WorkspaceSettings;
import v9t9.server.tcf.EmulatorTCFServer;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public abstract class EmulatorServerBase {

	static {
		MachineModelFactory.INSTANCE.register(
				StandardMachineModel.ID, StandardMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				EnhancedTI994AMachineModel.ID, EnhancedTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				Enhanced48KForthTI994AMachineModel.ID, Enhanced48KForthTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				F99bMachineModel.ID, F99bMachineModel.class);
	}
	
	private IMemory memory;
	private IMachine machine;
	private IMemoryModel memoryModel;
	private IClient client;
	private boolean inited;
	private ISettingsHandler settings;
	private IProperty bootRomsPath;
	private IProperty storedRamPath;
	private EmulatorTCFServer server;

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
			settings.getWorkspaceSettings().setDirty(false);
			WorkspaceSettings.loadFrom(settings.getWorkspaceSettings(),
					"workspace." + machine.getModel().getIdentifier());
		} catch (IOException e) {
		}
    	
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getErrorCount();
		
		
		memoryModel.loadMemory(client.getEventNotifier());
		
		if (machine.getModuleManager() != null) {
			machine.getModuleManager().reload();
		}
		
		if (client.getEventNotifier().getErrorCount() > barrier) {
			IStoredSettings storedSettings = settings.findSettingStorage(bootRomsPath.getName()); 
			machine.notifyEvent(IEventNotifier.Level.ERROR,
					"Failed to load startup ROMs; please edit your " + bootRomsPath.getName() + " in '"
					+ storedSettings.getConfigFilePath() + "'");
		}
	}
	
    public void init(String modelId) throws IOException {
    	if (bootRomsPath.getList().isEmpty())
    		bootRomsPath.getList().add(
    			settings.getInstanceSettings().getConfigDirectory() + "roms");
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


	/**
	 * @return
	 */
	public MachineModelFactory getMachineModelFactory() {
		return MachineModelFactory.INSTANCE;
	}

}
