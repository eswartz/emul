/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.HybridDemoClient;
import v9t9.emulator.clients.builtin.PureJavaClient;
import v9t9.emulator.hardware.memory.StandardConsoleMemoryModel;
import v9t9.emulator.runtime.AbortedException;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.Speech;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Gpl;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;

public class TI994A extends Machine {

	static {
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/roms");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/modules");
		DataFiles.addSearchPath("l:/src/v9t9-data/roms");
		DataFiles.addSearchPath("l:/src/v9t9-data/modules");
	}

    public TI994A() throws IOException {
        super(new StandardConsoleMemoryModel());
        getSettings().register(StandardConsoleMemoryModel.settingExpRam);
        getSettings().register(StandardConsoleMemoryModel.settingEnhRam);
    }
    
    @Override
	public void setClient(Client client) {
        super.setClient(client);
        
        getVdpMmio().setClient(client);
        getGplMmio().setClient(client);
        getSoundMmio().setClient(client);
        getSpeechMmio().setClient(client);
    }
    
    public v9t9.emulator.runtime.Sound getSoundMmio() {
        return ((StandardConsoleMemoryModel) memoryModel).soundMmio;
    }
    public v9t9.emulator.runtime.Vdp getVdpMmio() {
        return ((StandardConsoleMemoryModel) memoryModel).vdpMmio;
    }
    public Gpl getGplMmio() {
        return ((StandardConsoleMemoryModel) memoryModel).gplMmio;
    }
    public Speech getSpeechMmio() {
    	return ((StandardConsoleMemoryModel) memoryModel).speechMmio;
    }
    
    protected void loadConsoleRom(String filename) throws IOException {
    	DiskMemoryEntry cpuRomEntry = DiskMemoryEntry.newWordMemoryFromFile(0x0, 0x2000, "CPU ROM",
        		console,
                filename, 0x0, false);
    	cpuRomEntry.area.readByteLatency = cpuRomEntry.area.readWordLatency = 0;
    	cpuRomEntry.area.writeByteLatency = cpuRomEntry.area.writeWordLatency = 0;
		memory.addAndMap(cpuRomEntry);
    }
    protected void loadConsoleGrom(String filename) throws IOException {
    	memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x0, 0x6000, "CPU GROM", 
    			 ((StandardConsoleMemoryModel) memoryModel).GRAPHICS,
    			filename, 0x0, false));
    }

    protected void loadModuleRom(String name, String filename) throws IOException {
    	memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, 
    			name, console,
    			filename, 0x0, false));
    }
    protected void loadBankedModuleRom(String name, String filename1, String filename2) throws IOException {
    	memory.addAndMap(BankedMemoryEntry.newBankedWordMemoryFromFile(
    			memory,
    			name, console,
    			filename1, 0x0, 
    			filename2, 0x0));
    	
    }
    protected void loadModuleGrom(String name, String filename) throws IOException {
    	memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, name, 
    			((StandardConsoleMemoryModel) memoryModel).GRAPHICS,
    			filename, 0x0, false));
    	
    }
    
    @Override
	protected void loadMemory() throws IOException {
    	loadConsoleRom("994arom.bin");
    	loadConsoleGrom("994agrom.bin");

    	loadBankedModuleRom("ExtBasic", "tiextc.bin", "tiextd.bin");
    	loadModuleGrom("ExtBasic", "tiextg.bin");

    	loadModuleGrom("Parsec", "parsecg.bin");
    	loadModuleRom("Parsec", "parsecc.bin");
    	
    	//loadBankedModuleRom("Jungle", "junglec.bin", "jungled.bin");
    }

    @Override
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
        	Executor.settingDumpInstructions.setBoolean(true);
        	Executor.settingDumpFullInstructions.setBoolean(true);
        	//Compiler.settingDebugInstructions.setBoolean(true);
        }
        
        
    	StandardConsoleMemoryModel.settingExpRam.setBoolean(true);
    }
    
    public static void main(String args[]) throws IOException {
    	final Display display = new Display();
    	
        final TI994A machine = new TI994A();
        
        //machine.setClient(new DemoClient(machine));
        Client client;
        
        if (args.length >= 1 && args[0].equals("--pure"))
        	client = new PureJavaClient(machine, machine.getVdpMemoryDomain(), display);
        else
        	client = new HybridDemoClient(machine, machine.getVdpMemoryDomain(), display);
		machine.setClient(client);

        machine.getCpu().contextSwitch(0);
        
        Thread runner = new Thread("9900 Runner") {
        	@Override
        	public void run() {
        		try {
        	        while (machine.isRunning()) {
        	            try {
        	                machine.run();
        	            } catch (AbortedException e) {
        	                
        	            } catch (Throwable t) {
        	            	machine.setNotRunning();
        	            	break;
        	            }
        	        }
                } finally {
                	machine.close();
                }
        	}
        };

        runner.start();
        
        while (!display.isDisposed()) {
	    	while (display.readAndDispatch()) {
	    		// handle events
	    	}
	    	
	    	if (!machine.isRunning())
	    		break;
	    	
	    	// don't eat up CPU
	    	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
        }
        
    }

    public MemoryModel getMemoryModel() {
        return memoryModel;
    }

	public MemoryDomain getGplMemoryDomain() {
		return ((StandardConsoleMemoryModel) memoryModel).GRAPHICS;
	}
	public MemoryDomain getSpeechMemoryDomain() {
		return ((StandardConsoleMemoryModel) memoryModel).SPEECH;
	}
	public MemoryDomain getVdpMemoryDomain() {
		return ((StandardConsoleMemoryModel) memoryModel).VIDEO;
	}
    

}

