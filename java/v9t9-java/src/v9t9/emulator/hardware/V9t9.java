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

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.awt.AwtJavaClient;
import v9t9.emulator.clients.builtin.awt.AwtKeyboardHandler;
import v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer;
import v9t9.emulator.clients.builtin.swt.SwtAwtVideoRenderer;
import v9t9.emulator.clients.builtin.swt.SwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtKeyboardHandler;
import v9t9.emulator.clients.builtin.swt.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.memory.EnhancedConsoleMemoryModel;
import v9t9.emulator.hardware.memory.ExpRamArea;
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
import v9t9.engine.memory.WordMemoryArea;

public class V9t9 {

	static public final Setting settingMonitorDrawing = new Setting("MonitorDrawing", new Boolean(true));

	static {
		DataFiles.addSearchPath("/usr/local/src/V9t9/tools/Forth");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/roms");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/modules");
		DataFiles.addSearchPath("l:/src/V9t9/tools/Forth");
		DataFiles.addSearchPath("l:/src/v9t9-data/roms");
		DataFiles.addSearchPath("l:/src/v9t9-data/modules");
		DataFiles.addSearchPath("/tmp");
		DataFiles.addSearchPath("M:/fun/tidisk/eddie18");
		DataFiles.addSearchPath("/media/M/fun/tidisk/eddie18");
		DataFiles.addSearchPath("c:/devel/tistuff/v9t9-data/modules");
		DataFiles.addSearchPath("c:/devel/tistuff/v9t9-data/roms");
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
	
	    	if (false) {
	    		ExpRamArea.settingExpRam.setBoolean(true);
	    		loadModuleGrom("E/A", "eag.bin");
	    		DiskMemoryEntry entry = DiskMemoryEntry.newWordMemoryFromFile(0xA000, 0x6000, "Ed's BASIC",
	            		console,
	                    "ed_basicH.bin", 0x0, false);
	    		entry.load();
	        	entry.getArea().setLatency(4);
	        	short[] memory = ((WordMemoryArea) entry.getArea()).memory;
				for (int a = 0xA000; a < 0x10000; a+=2) {
	        		console.writeWord(a, memory[(a - 0xA000) / 2]);
	        	}
	    		
	    		entry = DiskMemoryEntry.newWordMemoryFromFile(0x2000, 0x2000, "Ed's BASIC",
	            		console,
	                    "ed_basicL.bin", 0x0, false);
	    		entry.load();
	        	entry.getArea().setLatency(4);
	        	memory = ((WordMemoryArea) entry.getArea()).memory;
	        	for (int a = 0x2000; a < 0x4000; a+=2) {
	        		console.writeWord(a, memory[(a - 0x2000) / 2]);
	        	}

	    	} else if (true) {
		    	
		    	//loadBankedModuleRom("Jungle", "junglec.bin", "jungled.bin");
	    		loadModuleRom("Alpiner", "alpinerc.bin");
	    		loadModuleGrom("Alpiner", "alpinerg.bin");
	    		loadModuleGrom("Mash", "mashg.bin");
	    		loadModuleRom("Mash", "mashc.bin");
	    		
	    		loadBankedModuleRom("ExtBasic", "tiextc.bin", "tiextd.bin");
	    		loadModuleGrom("ExtBasic", "tiextg.bin");
	    		loadModuleGrom("Parsec", "parsecg.bin");
	    		loadModuleRom("Parsec", "parsecc.bin");
	    		loadModuleGrom("TEII", "teiig.bin");
	    		loadModuleRom("TEII", "teiic.bin");
	    		loadModuleGrom("Music", "musicmg.bin");
	    		
	    	} else {
		    	loadModuleRom("Logo", "logoc.bin");
		    	loadModuleGrom("Logo", "logog.bin");
		    	//loadModuleRom("PRK", "prkc.bin");
		    	loadModuleGrom("PRK", "prkg.bin");
		    	ExpRamArea.settingExpRam.setBoolean(true);
	    	}
    	} else { 

    		// enhanced model can only load FORTH for now
    		DiskMemoryEntry entry;
    		
    		
    		loadEnhancedBankedConsoleRom("nforthA.rom", "nforthB.rom");
    		loadConsoleGrom("nforth.grm");
    		entry = loadModuleGrom("FORTH", "nforthg.bin");
    		
    		
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
	    	Compiler.settingOptimize.setBoolean(true);
	        //Compiler.settingOptimizeRegAccess.setBoolean(true);
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
        app.loadMemory();
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

