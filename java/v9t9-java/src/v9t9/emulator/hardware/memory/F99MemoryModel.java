package v9t9.emulator.hardware.memory;

import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
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
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		
	}
	
	protected void defineConsoleMemory(Machine machine) {
	    MemoryEntry entry = new MemoryEntry("64K RAM", CPU, 
	    		0x0000, 0x10000, new EnhancedRamArea(0, 0x10000));
	    entry.getArea().setLatency(0);
		memory.addAndMap(entry);
	}
	
	protected void defineMmioMemory(Machine machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0xFC00, 0x0400,
                new F99ConsoleMmioArea(machine)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
}
