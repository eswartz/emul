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
import v9t9.emulator.runtime.AbortedException;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Gpl;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.StandardConsoleMemoryModel;

public class TI994A extends Machine {

	static {
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/roms");
		DataFiles.addSearchPath("/usr/local/src/v9t9-data/modules");
		DataFiles.addSearchPath("l:/src/v9t9-data/roms");
		DataFiles.addSearchPath("l:/src/v9t9-data/modules");
	}
    StandardConsoleMemoryModel memoryModel;
    
    public TI994A() throws IOException {
        super();
    }
    
    @Override
	protected void createMemory() {
        memory = new Memory();
        memoryModel = new StandardConsoleMemoryModel(memory);
        CPU = memoryModel.CPU;
        getSettings().register(StandardConsoleMemoryModel.settingExpRam);
        getSettings().register(StandardConsoleMemoryModel.settingEnhRam);
    }

    @Override
	public void setClient(Client client) {
        super.setClient(client);
        memoryModel.connectClient(client);
    }
    
    public v9t9.emulator.runtime.Sound getSoundMmio() {
        return memoryModel.soundMmio;
    }
    public void setSoundMmio(v9t9.emulator.runtime.Sound soundMmio) {
        this.memoryModel.soundMmio = soundMmio;
    }
    public v9t9.emulator.runtime.Vdp getVdpMmio() {
        return memoryModel.vdpMmio;
    }
    public void setVdpMmio(v9t9.emulator.runtime.Vdp vdpMmio) {
        this.memoryModel.vdpMmio = vdpMmio;
    }
    public Gpl getGplMmio() {
        return memoryModel.gplMmio;
    }
    public void setGplMmio(Gpl gplMmio) {
        this.memoryModel.gplMmio = gplMmio;
    }

    @Override
	protected void loadMemory() throws IOException {
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x0, 0x2000, "CPU ROM", memoryModel.CPU,
                "994arom.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x0, 0x6000, "CPU GROM", memoryModel.GRAPHICS,
                "994agrom.bin", 0x0, false));
 
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "ExtBasic", memoryModel.GRAPHICS,
        		"tiextg.bin", 0x0, false));
        memory.addAndMap(BankedMemoryEntry.newBankedWordMemoryFromFile(
        		memory,
        		"ExtBasic", memoryModel.CPU,
        		"tiextc.bin", 0x0, 
        		"tiextd.bin", 0x0));

        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Parsec", memoryModel.GRAPHICS,
                "parsecg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Parsec", memoryModel.CPU,
                "parsecc.bin", 0x0, false));

        //memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Mini", memoryModel.GRAPHICS,
                //"minig.bin", 0x0, false));
        //memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Mini", memoryModel.CPU,
          //      "minic.bin", 0x0, false));
//      memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Logo", memoryModel.GRAPHICS,
//    		  "logog.bin", 0x0, false));
//      memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Logo", memoryModel.CPU,
//    		  "logoc.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Diags", memoryModel.GRAPHICS,
                "diagsg.bin", 0x0, false));


        memory.addAndMap(BankedMemoryEntry.newBankedWordMemoryFromFile(
        		memory,
        		"Jungle_Hunt", memoryModel.CPU,
        		"junglec.bin", 0x0, 
        		"jungled.bin", 0x0));
       // memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Carwars", memoryModel.GRAPHICS,
        //        "carwarsg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Parsec", memoryModel.GRAPHICS,
                "parsecg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Parsec", memoryModel.CPU,
                "parsecc.bin", 0x0, false));

        /*
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Tomb", memoryModel.GRAPHICS,
                "trsureg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Tomb", memoryModel.CPU,
                "trsurec.bin", 0x0, false));
*/


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
        	Compiler.settingDebugInstructions.setBoolean(true);
        }
        
        
    	StandardConsoleMemoryModel.settingExpRam.setBoolean(true);
    }
    
    public static void main(String args[]) throws IOException {
    	final Display display = new Display();
    	
        final TI994A machine = new TI994A();
        
        StandardConsoleMemoryModel memoryModel = machine.getMemoryModel();
        //machine.setClient(new DemoClient(machine));
        Client client;
        
        if (args.length >= 1 && args[0].equals("--pure"))
        	client = new PureJavaClient(machine, memoryModel.VIDEO, display);
        else
        	client = new HybridDemoClient(machine, memoryModel.VIDEO, display);
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
	    	
	    	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
	    	Thread.yield();
        }
        
    }

    public StandardConsoleMemoryModel getMemoryModel() {
        return memoryModel;
    }
    

}

