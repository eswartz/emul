/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.server;

import java.io.IOException;


import v9t9.common.client.IClient;
import v9t9.common.compiler.ICompiler;
import v9t9.common.cpu.ICpu;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryModel;
import v9t9.engine.machine.MachineModelFactory;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.modules.ModuleManager;
import v9t9.engine.settings.EmulatorSettings;
import v9t9.engine.settings.WorkspaceSettings;
import v9t9.machine.f99b.machine.F99bMachineModel;
import v9t9.machine.ti99.machine.Enhanced48KForthTI994AMachineModel;
import v9t9.machine.ti99.machine.StandardMachineModel;
import v9t9.machine.ti99.machine.EnhancedTI994AMachineModel;

public class EmulatorServer {

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

    public EmulatorServer() {
		IVdpChip.settingDumpVdpAccess.setBoolean(true);
		GplMmio.settingDumpGplAccess.setBoolean(true);
    }
    
    /**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}
	protected void setupDefaults() {
    	WorkspaceSettings.CURRENT.register(IMachine.settingModuleList);
		WorkspaceSettings.CURRENT.register(ICpu.settingCyclesPerSecond);
		WorkspaceSettings.CURRENT.register(ICpu.settingRealTime);
		
		try {
			WorkspaceSettings.CURRENT.setDirty(false);
			WorkspaceSettings.loadFrom("workspace." + machine.getModel().getIdentifier());
		} catch (IOException e) {
		}
		
    	ICpu.settingRealTime.setBoolean(true);
    	
    	// compile defaults
    	//CompilerBase.settingDebugInstructions.setBoolean(true);
    	//CompilerBase.settingOptimize.setBoolean(true);
        ICompiler.settingOptimizeRegAccess.setBoolean(true);
        ICompiler.settingOptimizeStatus.setBoolean(true);
        //CompilerBase.settingCompileOptimizeCallsWithData.setBoolean(true);
        //CompilerBase.settingCompileFunctions.setBoolean(true);
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getErrorCount();
		memoryModel.loadMemory(client.getEventNotifier());
		
		if (machine.getModuleManager() != null) {
			try {
	    		String dbNameList = IMachine.settingModuleList.getString();
	    		if (dbNameList.length() > 0) {
	    			String[] dbNames = dbNameList.split(";");
	    			machine.getModuleManager().loadModules(dbNames, client.getEventNotifier());
	    		}
	        	if (ModuleManager.settingLastLoadedModule.getString().length() > 0)
	        		machine.getModuleManager().switchModule(ModuleManager.settingLastLoadedModule.getString());
	        	
	        } catch (NotifyException e) {
	        	machine.notifyEvent(e.getEvent());
	        }
	        
		}
		
		if (client.getEventNotifier().getErrorCount() > barrier) {
			machine.notifyEvent(IEventNotifier.Level.ERROR,
					"Failed to load startup ROMs; please edit your " + DataFiles.settingBootRomsPath.getName() + " in the file "
					+ WorkspaceSettings.CURRENT.getConfigFilePath());
			//EmulatorSettings.INSTANCE.save();
		}
	}
	
    public void init(String modelId) throws IOException {
    	
    	try {
    		EmulatorSettings.INSTANCE.load();
    	} catch (IOException e) {
    		System.err.println("Setting up new instance");
    	}

		WorkspaceSettings.CURRENT.register(ModuleManager.settingLastLoadedModule);

		EmulatorSettings.INSTANCE.register(DataFiles.settingBootRomsPath,
				EmulatorSettings.INSTANCE.getConfigDirectory() + "roms");
		EmulatorSettings.INSTANCE.register(DataFiles.settingStoredRamPath,
				EmulatorSettings.INSTANCE.getConfigDirectory() + "module_ram");
		DataFiles.addSearchPath(DataFiles.settingStoredRamPath.getString());
    	
    	try {
    		WorkspaceSettings.loadFrom(WorkspaceSettings.currentWorkspace.getString());
    	} catch (IOException e) {
    		System.err.println("Setting up new configuration");
    	}
    	
        IMachineModel model = MachineModelFactory.INSTANCE.createModel(modelId);
        assert (model != null);
        
        machine = model.createMachine();
        
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();

    }

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
		
		WorkspaceSettings.CURRENT.save();        	
    	EmulatorSettings.INSTANCE.save();   
	}

	public void run() {
		
		if (!inited) {
			inited = true;
	        setupDefaults();
	        loadState();
		}
		
		machine.reset();
        machine.start();
        
        while (client.isAlive()) {
        	client.handleEvents();
	    	
	    	if (!machine.isAlive())
	    		break;
	    	
	    	// don't eat up CPU
	    	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
        }
	}

	/**
	 * @return
	 */
	public MachineModelFactory getMachineModelFactory() {
		return MachineModelFactory.INSTANCE;
	}

}

