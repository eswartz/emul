package v9t9.emulator.hardware.memory;

import java.io.IOException;

import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryEntry;


/**
 * F99 console memory model.
 * @author ejs
 */
public class F99MemoryModel extends TI994AStandardConsoleMemoryModel {

	public F99MemoryModel() {
		super();
	}

	@Override
	protected void initSettings() {
		DataFiles.addSearchPath("../v9t9.forthcomp/data");
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		DiskMemoryEntry cpuRomEntry;
		String filename = "f99rom.bin";
    	try {
			cpuRomEntry = DiskMemoryEntry.newWordMemoryFromFile(
	    			0x0, 0x2000, "CPU ROM",
	        		CPU,
	                filename, 0x0, false);
			cpuRomEntry.load();
			for (int i = 0; i < cpuRomEntry.size; i += 2)
				CPU.writeWord(i, cpuRomEntry.readWord(i));
			cpuRomEntry.copySymbols(CPU);
			
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename, e);
    	}
    	
		loadConsoleGrom(eventNotifier, "nforth.grm");
	}
	
	protected void defineConsoleMemory(Machine machine) {
	    MemoryEntry entry = new MemoryEntry("64K RAM", CPU, 
	    		0x0400, 0xFC00, new EnhancedRamArea(0, 0xFC00));
	    entry.getArea().setLatency(0);
		memory.addAndMap(entry);
	}
	
	protected void defineMmioMemory(Machine machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0x0000, 0x0400,
                new F99ConsoleMmioArea(machine)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
}
