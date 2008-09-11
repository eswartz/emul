/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import org.eclipse.swt.widgets.Display;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.HybridDemoClient;
import v9t9.emulator.runtime.AbortedException;
import v9t9.emulator.runtime.Compiler;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.Client;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Gpl;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.StandardConsoleMemoryModel;

public class TI994A extends Machine {

    StandardConsoleMemoryModel memoryModel;
    
    public TI994A() {
        super();
    }
    
    @Override
	protected void createMemory() {
        memory = new Memory();
        memoryModel = new StandardConsoleMemoryModel(getClient(), memory);
        CPU = memoryModel.CPU;
        getSettings().register(StandardConsoleMemoryModel.settingExpRam);
        getSettings().register(StandardConsoleMemoryModel.settingEnhRam);
    }

    @Override
	public void setClient(Client client) {
        super.setClient(client);
        memoryModel.setClient(client);
    }
    
    public v9t9.engine.memory.Sound getSoundMmio() {
        return memoryModel.soundMmio;
    }
    public void setSoundMmio(v9t9.engine.memory.Sound soundMmio) {
        this.memoryModel.soundMmio = soundMmio;
    }
    public v9t9.engine.memory.Vdp getVdpMmio() {
        return memoryModel.vdpMmio;
    }
    public void setVdpMmio(v9t9.engine.memory.Vdp vdpMmio) {
        this.memoryModel.vdpMmio = vdpMmio;
    }
    public Gpl getGplMmio() {
        return memoryModel.gplMmio;
    }
    public void setGplMmio(Gpl gplMmio) {
        this.memoryModel.gplMmio = gplMmio;
    }

    @Override
	protected void loadMemory() {
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x0, 0x2000, "CPU ROM", memoryModel.CPU,
                "/usr/local/src/v9t9-data/roms/994arom.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x0, 0x6000, "CPU GROM", memoryModel.GRAPHICS,
                "/usr/local/src/v9t9-data/roms/994agrom.bin", 0x0, false));
 
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "ExtBasic", memoryModel.GRAPHICS,
        		"/usr/local/src/v9t9-data/modules/tiextg.bin", 0x0, false));
        memory.addAndMap(BankedMemoryEntry.newBankedWordMemoryFromFile(
        		memory,
        		"ExtBasic", memoryModel.CPU,
        		"/usr/local/src/v9t9-data/modules/tiextc.bin", 0x0, 
        		"/usr/local/src/v9t9-data/modules/tiextd.bin", 0x0));

        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Parsec", memoryModel.GRAPHICS,
                "/usr/local/src/v9t9-data/modules/parsecg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Parsec", memoryModel.CPU,
                "/usr/local/src/v9t9-data/modules/parsecc.bin", 0x0, false));

        //memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Mini", memoryModel.GRAPHICS,
                //"/usr/local/src/v9t9-data/modules/minig.bin", 0x0, false));
        //memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Mini", memoryModel.CPU,
          //      "/usr/local/src/v9t9-data/modules/minic.bin", 0x0, false));
//      memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Logo", memoryModel.GRAPHICS,
//    		  "/usr/local/src/v9t9-data/modules/logog.bin", 0x0, false));
//      memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Logo", memoryModel.CPU,
//    		  "/usr/local/src/v9t9-data/modules/logoc.bin", 0x0, false));

        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Diags", memoryModel.GRAPHICS,
             "/usr/local/src/v9t9-data/modules/diagsg.bin", 0x0, false));
        memory.addAndMap(BankedMemoryEntry.newBankedWordMemoryFromFile(
        		memory,
        		"Jungle_Hunt", memoryModel.CPU,
        		"/usr/local/src/v9t9-data/modules/junglec.bin", 0x0, 
        		"/usr/local/src/v9t9-data/modules/jungled.bin", 0x0));
        memory.addAndMap(DiskMemoryEntry.newByteMemoryFromFile(0x6000, 0, "Parsec", memoryModel.GRAPHICS,
                "/usr/local/src/v9t9-data/modules/parsecg.bin", 0x0, false));
        memory.addAndMap(DiskMemoryEntry.newWordMemoryFromFile(0x6000, 0, "Parsec", memoryModel.CPU,
                "/usr/local/src/v9t9-data/modules/parsecc.bin", 0x0, false));


    }

    @Override
	protected void setupDefaults() {
    	Executor.settingCompile.setBoolean(true);
        Compiler.settingOptimize.setBoolean(true);
        Compiler.settingOptimizeRegAccess.setBoolean(true);
        Compiler.settingOptimizeStatus.setBoolean(true);
        Compiler.settingCompileOptimizeCallsWithData.setBoolean(true);
        
        if (false) {
        	Executor.settingDumpInstructions.setBoolean(true);
        	Executor.settingDumpFullInstructions.setBoolean(true);
        	Compiler.settingDebugInstructions.setBoolean(true);
        }
        
        Compiler.settingCompileFunctions.setBoolean(true);
        
    	StandardConsoleMemoryModel.settingExpRam.setBoolean(true);
    }
    
    public static void main(String args[]) {
    	final Display display = new Display();
    	
        final TI994A machine = new TI994A();
        //machine.setClient(new DemoClient(machine));
        HybridDemoClient client = new HybridDemoClient(machine, machine.getVdpMmio().getMemory(), display);
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
        }
        
    }

    public StandardConsoleMemoryModel getMemoryModel() {
        return memoryModel;
    }
    

}

