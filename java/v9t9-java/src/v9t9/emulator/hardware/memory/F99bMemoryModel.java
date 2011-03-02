package v9t9.emulator.hardware.memory;

import java.io.IOException;

import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryEntry;


/**
 * F99b console memory model.
 * @author ejs
 */
public class F99bMemoryModel extends BaseTI994AMemoryModel {

	public F99bMemoryModel() {
		super();
	}

	@Override
	protected void initSettings() {
		DataFiles.addSearchPath("../../tools/Forth99/bin");
		DataFiles.addSearchPath("../../tools/Forth99/");
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		DiskMemoryEntry cpuRomEntry;
		String filename = "f99brom.bin";
    	try {
			cpuRomEntry = DiskMemoryEntry.newByteMemoryFromFile(
	    			0x400, 0, "CPU ROM",
	        		CPU,
	                filename, 0x400, false);
			cpuRomEntry.load();
			memory.addAndMap(cpuRomEntry);
			cpuRomEntry.copySymbols(CPU);
			
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename, e);
    	}
    	
		loadConsoleGrom(eventNotifier, "forth99.grm");
		
		DiskMemoryEntry entry;
		try {
			entry = DiskMemoryEntry.newByteMemoryFromFile(
	    			0x2000, 0xE000, "GRAM", 
	    			GRAPHICS,
	    			"f99bgram.bin", 0x0, true);
			memory.addAndMap(entry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, filename, e);
		}
	}
	
	protected void defineConsoleMemory(Machine machine) {
		MemoryEntry entry = new MemoryEntry("64K RAM", CPU, 
				0x0400, 0xFC00, new EnhancedRamByteArea(0x4000, 0xFC00));
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
