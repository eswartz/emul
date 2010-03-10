/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;


import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.Machine;
import v9t9.emulator.ModuleManager;
import v9t9.emulator.clients.builtin.awt.AwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtJavaClient;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper;
import v9t9.emulator.hardware.memory.EnhancedConsoleMemoryModel;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;

public class V9t9 {

	static {
		DataFiles.settingBootRomsPath.addListener(new ISettingListener() {
			
			@Override
			public void changed(Setting setting, Object oldValue) {
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
	private MemoryDomain console;
	private MemoryModel memoryModel;
	private Client client;

    public V9t9(Machine machine, Client client) throws IOException {
    	this.machine = machine;
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();
    	this.console = memoryModel.getConsole();
    	
    	this.client = client;
    	machine.setClient(client);
    }
    
    protected DiskMemoryEntry loadConsoleRom(String filename) throws IOException {
    	DiskMemoryEntry cpuRomEntry = DiskMemoryEntry.newWordMemoryFromFile(0x0, 0x2000, "CPU ROM",
        		console,
                filename, 0x0, false);
    	cpuRomEntry.getArea().setLatency(0);
		memory.addAndMap(cpuRomEntry);
		return cpuRomEntry;
    }
    protected BankedMemoryEntry loadBankedConsoleRom(String filename1, String filename2) throws IOException {
    	BankedMemoryEntry cpuRomEntry = DiskMemoryEntry.newWriteTogglingBankedWordMemoryFromFile(
    			0x0000,
    			0x2000,
    			memory,
    			"CPU ROM", console,
    			filename1, 0x0, filename2, 0x0);
    	cpuRomEntry.getArea().setLatency(0);
    	memory.addAndMap(cpuRomEntry);
    	return cpuRomEntry;
    }
    protected DiskMemoryEntry loadConsoleGrom(String filename) throws IOException {
    	DiskMemoryEntry entry = DiskMemoryEntry.newByteMemoryFromFile(0x0, 0x6000, "CPU GROM", 
    			memory.getDomain("GRAPHICS"),
    			filename, 0x0, false);
		memory.addAndMap(entry);
		return entry;
    }

    protected DiskMemoryEntry loadModuleRom(String name, String filename) throws IOException {
    	DiskMemoryEntry entry = DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0x2000, 
    			name, console,
    			filename, 0x0, false);
		memory.addAndMap(entry);
		return entry;
    }
    protected BankedMemoryEntry loadBankedModuleRom(String name, String filename1, String filename2) throws IOException {
    	BankedMemoryEntry entry = DiskMemoryEntry.newWriteTogglingBankedWordMemoryFromFile(
    			0x6000,
    			0x2000, memory,
    			name, console,
    			filename1, 0x0, 
    			filename2, 0x0);
		memory.addAndMap(entry);
    	return entry;
    }
    protected DiskMemoryEntry loadModuleGrom(String name, String filename) throws IOException {
    	DiskMemoryEntry entry = DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, name, 
    			memory.getDomain("GRAPHICS"),
    			filename, 0x0, false);
		memory.addAndMap(entry);
		return entry;
    }
    
	protected void loadMemory() throws IOException {
		if (!(memoryModel instanceof EnhancedConsoleMemoryModel)) {
	    	loadConsoleRom("994arom.bin");
	    	loadConsoleGrom("994agrom.bin");
	
    	} else { 

    		// enhanced model can only load FORTH for now
    		DiskMemoryEntry entry;
    		
    		loadEnhancedBankedConsoleRom("nforthA.rom", "nforthB.rom");
    		loadConsoleGrom("nforth.grm");
    		entry = loadModuleGrom("FORTH", "nforthg.bin");
    		
    		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK1", new File("../../tools/Forth"));
    		
    		// the high-GROM code is copied into RAM here
    		try {
	    		console.getEntryAt(0x6000).loadSymbols(
	    				new FileInputStream(DataFiles.resolveFile(entry.getSymbolFilepath())));
    		} catch (IOException e) {
    			
    		}
    	}
    }

    protected BankedMemoryEntry loadEnhancedBankedConsoleRom(String filename1, String filename2) throws IOException {
    	// not toggled based on writes to the ROM, but MMIO
    	BankedMemoryEntry cpuRomEntry = DiskMemoryEntry.newBankedWordMemoryFromFile(
    			0x0000,
    			0x4000,
    			memory,
    			"CPU ROM (enhanced)", console,
    			filename1, 0x0, filename2, 0x0);
    	cpuRomEntry.getArea().setLatency(0);
    	memory.addAndMap(cpuRomEntry);
    	return cpuRomEntry;
    }

	protected void setupDefaults() {
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
        try {
        	app.loadMemory();
        	
        	EmulatorSettings.INSTANCE.register(ModuleManager.settingLastLoadedModule);
        	if (ModuleManager.settingLastLoadedModule.getString().length() > 0)
        		machine.getModuleManager().switchModule(ModuleManager.settingLastLoadedModule.getString());
        	
        } catch (IOException e) {
        	machine.notifyEvent("Failed to load startup ROMs; please edit your BootRomsPath in the file "
        		+ EmulatorSettings.INSTANCE.getSettingsConfigurationPath());
        	//DataFiles.saveState(EmulatorSettings.getInstance().getApplicationSettings());
        	EmulatorSettings.INSTANCE.save();
        }
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

