/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.HybridDemoClient;
import v9t9.emulator.clients.builtin.PureJavaClient;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.memory.ExpRamArea;
import v9t9.emulator.hardware.memory.StandardConsoleMemoryModel;
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

	static {
		DataFiles.addSearchPath("/usr/local/src/V9t9/tools/Forth");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/roms");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/modules");
		DataFiles.addSearchPath("l:/src/V9t9/tools/Forth");
		DataFiles.addSearchPath("l:/src/v9t9-data/roms");
		DataFiles.addSearchPath("l:/src/v9t9-data/modules");
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
    	cpuRomEntry.area.setLatency(0);
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
    	cpuRomEntry.area.setLatency(0);
    	memory.addAndMap(cpuRomEntry);
    	return cpuRomEntry;
    }
    protected DiskMemoryEntry loadConsoleGrom(String filename) throws IOException {
    	DiskMemoryEntry entry = DiskMemoryEntry.newByteMemoryFromFile(0x0, 0x6000, "CPU GROM", 
    			 ((StandardConsoleMemoryModel) memoryModel).GRAPHICS,
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
    			((StandardConsoleMemoryModel) memoryModel).GRAPHICS,
    			filename, 0x0, false);
		memory.addAndMap(entry);
		return entry;
    }
    
	protected void loadMemory() throws IOException {
    	if (true) {
	    	loadConsoleRom("994arom.bin");
	    	loadConsoleGrom("994agrom.bin");
	
	    	if (true) {
	    		loadModuleGrom("E/A", "eag.bin");
	    		DiskMemoryEntry entry = DiskMemoryEntry.newWordMemoryFromFile(0xA000, 0x6000, "Ed's BASIC",
	            		console,
	                    "ed_basicH.bin", 0x0, false);
	    		entry.load();
	        	entry.area.setLatency(4);
	        	for (int a = 0xA000; a < 0x10000; a+=2) {
	        		console.writeWord(a, ((WordMemoryArea) entry.area).memory[(a - 0xA000) / 2]);
	        	}
	    		
	    		entry = DiskMemoryEntry.newWordMemoryFromFile(0x2000, 0x2000, "Ed's BASIC",
	            		console,
	                    "ed_basicL.bin", 0x0, false);
	    		entry.load();
	        	entry.area.setLatency(4);
	        	for (int a = 0x2000; a < 0x4000; a+=2) {
	        		console.writeWord(a, ((WordMemoryArea) entry.area).memory[(a - 0x2000) / 2]);
	        	}

	    	} else {
		    	loadBankedModuleRom("ExtBasic", "tiextc.bin", "tiextd.bin");
		    	loadModuleGrom("ExtBasic", "tiextg.bin");
		    	
		    	loadModuleGrom("Parsec", "parsecg.bin");
		    	loadModuleRom("Parsec", "parsecc.bin");
		    	loadBankedModuleRom("Jungle", "junglec.bin", "jungled.bin");
	    	}
    	} 

    	if (true) {
    		DiskMemoryEntry entry;
    		loadBankedConsoleRom("nforthA.rom", "nforthB.rom");
    		loadConsoleGrom("nforth.grm");
    		loadModuleRom("FORTH", "nforthc.bin");
    		entry = loadModuleGrom("FORTH", "nforthg.bin");
    		
    		// the high-GROM code is copied into RAM here
    		console.getEntryAt(0xA000).loadSymbols(
    				new FileInputStream(DataFiles.resolveFile(entry.getSymbolFilepath())));
    	}
    }

	protected void setupDefaults() {
    	Cpu.settingRealTime.setBoolean(true);
    	if (false) {
    		Cpu.settingRealTime.setBoolean(false);
	    	Executor.settingCompile.setBoolean(true);
	        Compiler.settingOptimize.setBoolean(true);
	        Compiler.settingOptimizeRegAccess.setBoolean(true);
	        Compiler.settingOptimizeStatus.setBoolean(true);
	        Compiler.settingCompileOptimizeCallsWithData.setBoolean(true);
	        Compiler.settingCompileFunctions.setBoolean(true);
    	}
        
        if (false) {
        	//Executor.settingDumpInstructions.setBoolean(true);
        	Executor.settingDumpFullInstructions.setBoolean(true);
        	//Compiler.settingDebugInstructions.setBoolean(true);
        }
        if (true) {
        	VdpTMS9918A.settingDumpVdpAccess.setBoolean(true);
        }
        
    	ExpRamArea.settingExpRam.setBoolean(true);
    }
    
    public static void main(String args[]) throws IOException {
    	
        Machine machine;
        
        if (findArgument(args, "--enhanced")) {
        	machine = new TI994A(new EnhancedMachineModel());
        }
        else {
        	machine = new TI994A(new StandardMachineModel());
        }
        
        final Display display = new Display();
        Client client;
        if (findArgument(args, "--pure"))
        	client = new PureJavaClient(machine, machine.getVdp(), display);
        else
        	client = new HybridDemoClient(machine, machine.getVdp(), display);
        
        final V9t9 app = new V9t9(machine, client);
        
        app.setupDefaults();
        app.loadMemory();
        app.run();
        
    }

	private void run() {
		
		machine.getCpu().contextSwitch(0);
        machine.start();
        
        while (client.isAlive()) {
        	client.handleEvents();
	    	
	    	if (!machine.isRunning())
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

}

