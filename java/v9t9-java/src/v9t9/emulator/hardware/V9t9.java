/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import java.io.File;
import java.io.IOException;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.IEventNotifier;
import v9t9.emulator.Machine;
import v9t9.emulator.ModuleManager;
import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.emulator.clients.builtin.awt.AwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtJavaClient;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryModel;

public class V9t9 {

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


	private Memory memory;
	private Machine machine;
	private MemoryModel memoryModel;
	private Client client;

    public V9t9(Machine machine, Client client) throws IOException {
    	this.machine = machine;
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();
    	
    	this.client = client;
    	machine.setClient(client);
    }
    
	protected void setupDefaults() {
		EmulatorSettings.INSTANCE.register(Cpu.settingCyclesPerSecond);
		EmulatorSettings.INSTANCE.register(Cpu.settingRealTime);
		EmulatorSettings.INSTANCE.register(Machine.settingExpRam);

		
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
        
        if (false) {
        	//Executor.settingDumpInstructions.setBoolean(true);
        	Executor.settingDumpFullInstructions.setBoolean(true);
        	//Compiler.settingDebugInstructions.setBoolean(true);
        }
        if (false) {
        	VdpTMS9918A.settingDumpVdpAccess.setBoolean(true);
        }
        
    	Machine.settingExpRam.setBoolean(true);
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getNotificationCount();
		memoryModel.loadMemory(client.getEventNotifier());
		
		EmulatorSettings.INSTANCE.register(ModuleManager.settingLastLoadedModule);
		try {
        	if (ModuleManager.settingLastLoadedModule.getString().length() > 0)
        		machine.getModuleManager().switchModule(ModuleManager.settingLastLoadedModule.getString());
        	
        } catch (NotifyException e) {
        	machine.notifyEvent(e.getEvent());
        }
        
        if (client.getEventNotifier().getNotificationCount() > barrier) {
        	machine.notifyEvent(IEventNotifier.Level.ERROR,
        			"Failed to load startup ROMs; please edit your " + DataFiles.settingBootRomsPath.getName() + " in the file "
        		+ EmulatorSettings.INSTANCE.getSettingsConfigurationPath());
        	EmulatorSettings.INSTANCE.save();
        }
	}

    public static void main(String args[]) throws IOException {
    	
    	EmulatorSettings.INSTANCE.load();
		EmulatorSettings.INSTANCE.register(DataFiles.settingBootRomsPath);
		EmulatorSettings.INSTANCE.register(DataFiles.settingStoredRamPath);
    	
        Machine machine;
        
        if (findArgument(args, "--enhanced")) {
        	machine = new TI994A(new EnhancedMachineModel());
        }
        else {
        	machine = new TI994A(new StandardMachineModel());
        }
        
        Client client = createClient(args, machine);
        
        final V9t9 app = new V9t9(machine, client);
        
        app.setupDefaults();
        app.loadState();
        app.run();
        
    }

	private static Client createClient(String[] args, Machine machine) {
		Client client;
		
		/*
        else if (findArgument(args, "--sdl")) {
			client = new SdlJavaClient(machine, machine.getVdp()); 
        }
        */
        if (findArgument(args, "--awt")) {
			client = new AwtJavaClient(machine, machine.getVdp());
		} 
        else /*if (findArgument(args, "--swtawt"))*/ {
        	boolean awtRenderer = !findArgument(args, "--swt");
        	client = new SwtJavaClient(machine, machine.getVdp(), awtRenderer);
		} 
		/*
        else if (findArgument(args, "--swtsdl")) {
        	SwtSdlVideoRenderer videoRenderer;
			try {
				videoRenderer = new SwtSdlVideoRenderer();
			} catch (SDLException e) {
				throw (IOException) new IOException().initCause(e);
			}
			client = new SwtJavaClient(machine, machine.getVdp(), 
					videoRenderer, 
					new SdlKeyboardHandler(
			        		machine.getKeyboardState(), machine),
					display);
		} 
        else {
			client = new HybridDemoClient(machine, machine.getVdp(), display);
		}
		 */
		return client;
	}

	private void run() {
		
		machine.getCpu().contextSwitch(0);
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
		return new File(string);
	}

}

