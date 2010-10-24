/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.utils.Check;

import v9t9.emulator.clients.builtin.ClientFactory;
import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.emulator.clients.builtin.awt.AwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtAwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtJavaClient;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.common.EmulatorSettings;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.emulator.common.ModuleManager;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.EnhancedCompatibleMachineModel;
import v9t9.emulator.hardware.EnhancedMachineModel;
import v9t9.emulator.hardware.F99MachineModel;
import v9t9.emulator.hardware.MFP201MachineModel;
import v9t9.emulator.hardware.MachineModel;
import v9t9.emulator.hardware.MachineModelFactory;
import v9t9.emulator.hardware.StandardMachineModel;
import v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryModel;

public class Emulator {

	static {
		DataFiles.settingBootRomsPath.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				if (setting.getList().isEmpty())
					addDefaultPaths();
				//DataFiles.settingBootRomsPath.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}

		});
		
		addDefaultPaths();
	}
	
	private static void addDefaultPaths() {
		DataFiles.addSearchPath("../../build/roms");		
	}


	static {
		MachineModelFactory.register(StandardMachineModel.ID, StandardMachineModel.class);
		MachineModelFactory.register(EnhancedCompatibleMachineModel.ID, EnhancedCompatibleMachineModel.class);
		MachineModelFactory.register(EnhancedMachineModel.ID, EnhancedMachineModel.class);
		MachineModelFactory.register(MFP201MachineModel.ID, MFP201MachineModel.class);
		MachineModelFactory.register(F99MachineModel.ID, F99MachineModel.class);
		
		ClientFactory.register(SwtJavaClient.ID, SwtJavaClient.class);
		ClientFactory.register(SwtAwtJavaClient.ID, SwtAwtJavaClient.class);
		ClientFactory.register(AwtJavaClient.ID, AwtJavaClient.class);
	}
	
	private Memory memory;
	private Machine machine;
	private MemoryModel memoryModel;
	private Client client;

    public Emulator(Machine machine, Client client) throws IOException {
    	Check.checkArg(machine);
    	Check.checkArg(client);
    	this.machine = machine;
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();
    	
    	this.client = client;
    	machine.setClient(client);
    }
    
	protected void setupDefaults() {
		
		WorkspaceSettings.CURRENT.register(Cpu.settingCyclesPerSecond);
		WorkspaceSettings.CURRENT.register(Cpu.settingRealTime);
		WorkspaceSettings.CURRENT.register(TI994AStandardConsoleMemoryModel.settingExpRam);
		
    	Cpu.settingRealTime.setBoolean(true);
    	
    	// compile?  and waste a lot of effort to get nothing done?
    	if (false) {
    		Cpu.settingRealTime.setBoolean(false);
	    	Executor.settingCompile.setBoolean(true);
	    	//Compiler.settingDebugInstructions.setBoolean(true);
	    	//Compiler.settingOptimize.setBoolean(true);
	        Compiler.settingOptimizeRegAccess.setBoolean(true);
	        Compiler.settingOptimizeStatus.setBoolean(true);
	        //Compiler.settingCompileOptimizeCallsWithData.setBoolean(true);
	        //Compiler.settingCompileFunctions.setBoolean(true);
	        //Executor.settingDumpInstructions.setBoolean(true);
	        //Executor.settingDumpFullInstructions.setBoolean(true);
    	}
        
        if (true) {
        	//Executor.settingDumpInstructions.setBoolean(true);
        	Executor.settingDumpFullInstructions.setBoolean(true);
        	//Compiler.settingDebugInstructions.setBoolean(true);
        }
        if (false) {
        	VdpTMS9918A.settingDumpVdpAccess.setBoolean(true);
        }
        
    	TI994AStandardConsoleMemoryModel.settingExpRam.setBoolean(true);
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getNotificationCount();
		memoryModel.loadMemory(client.getEventNotifier());
		
		WorkspaceSettings.CURRENT.register(ModuleManager.settingLastLoadedModule);
		try {
        	if (ModuleManager.settingLastLoadedModule.getString().length() > 0)
        		machine.getModuleManager().switchModule(ModuleManager.settingLastLoadedModule.getString());
        	
        } catch (NotifyException e) {
        	machine.notifyEvent(e.getEvent());
        }
        
        if (client.getEventNotifier().getNotificationCount() > barrier) {
        	machine.notifyEvent(IEventNotifier.Level.ERROR,
        			"Failed to load startup ROMs; please edit your " + DataFiles.settingBootRomsPath.getName() + " in the file "
        		+ WorkspaceSettings.CURRENT.getConfigFilePath());
        	//EmulatorSettings.INSTANCE.save();
        }
	}
	
    public static void main(String args[]) throws IOException {
    	
    	EmulatorSettings.INSTANCE.load();
    	try {
    		WorkspaceSettings.loadFrom(WorkspaceSettings.currentWorkspace.getString());
    	} catch (IOException e) {
    		System.err.println("Setting up new configuration");
    	}
    	
		EmulatorSettings.INSTANCE.register(DataFiles.settingBootRomsPath);
		EmulatorSettings.INSTANCE.register(DataFiles.settingStoredRamPath);
    	
        Machine machine;
        
        String modelId = StandardMachineModel.ID;
        if (findArgument(args, "--f99")) {
        	modelId = F99MachineModel.ID;
        } else if (findArgument(args, "--mfp201")) {
        	modelId = MFP201MachineModel.ID;
        } else {
	        if (findArgument(args, "--enhanced")) {
	        	modelId = EnhancedMachineModel.ID;
	        }
	        else {
	        	modelId = StandardMachineModel.ID;
	        }
        }
        
        MachineModel model = MachineModelFactory.createModel(modelId);
        assert (model != null);
        	
        machine = model.createMachine();
        
        Client client = createClient(args, machine);
        
        Emulator.createAndRun(machine, client);
    }

	/**
	 * @param machine2
	 * @param client2
	 * @throws IOException 
	 */
	public static void createAndRun(Machine machine, Client client) throws IOException {
        final Emulator app = new Emulator(machine, client);
        
        app.setupDefaults();
        app.loadState();
        try {
        	app.run();
        } finally {
        	WorkspaceSettings.CURRENT.save();        	
        	EmulatorSettings.INSTANCE.save();        	
        }
		
	}

	private static Client createClient(String[] args, Machine machine) {
		String clientID;
        if (findArgument(args, "--awt")) {
        	clientID = AwtJavaClient.ID;
		} 
        else /*if (findArgument(args, "--swtawt"))*/ {
        	boolean awtRenderer = !findArgument(args, "--swt");
        	clientID = awtRenderer? SwtAwtJavaClient.ID : SwtJavaClient.ID;
		} 
		return ClientFactory.createClient(clientID, machine);
	}

	private void run() {
		
		machine.getCpu().reset();
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

 	private static boolean findArgument(String[] args, String string) {
    	for (String arg : args)
    		if (arg.equals(string))
    			return true;
		return false;
	}

	public static File getDataFile(String string) {
		File file = new File(string);
		if (file.exists())
			return file;
		// HACK
		return new File("../v9t9-java/" + string);
		/*
		Bundle bundle = Platform.getBundle("v9t9-java");
		URL url = FileLocator.find(bundle, new Path(string), Collections.emptyMap());
		if (url != null)
			return new File(url.toExternalForm());
		return new File(string);
		*/
	}

}

