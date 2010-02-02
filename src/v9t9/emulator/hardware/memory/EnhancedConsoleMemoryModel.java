/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.emulator.hardware.memory;

import java.util.List;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.TI994A;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.modules.IModule;


/**
 * Enhanced console memory model with a more sensible layout.
 * <p>
 * This has:
 * @author ejs
 */
public class EnhancedConsoleMemoryModel extends StandardConsoleMemoryModel {

	public EnhancedConsoleMemoryModel() {
		super();
	}

	@Override
	protected void initSettings() {
		ConsoleRamArea.settingEnhRam.setBoolean(true);
	}
	
	protected void defineConsoleMemory(Machine machine) {
	    MemoryEntry entry = new MemoryEntry("Super 48K expansion RAM", CPU, 
	    		0x4000, 0xC000, new EnhancedRamArea(0, 0xC000));
	    entry.getArea().setLatency(0);
		memory.addAndMap(entry);
	}
	
	protected void defineMmioMemory(TI994A machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0xFC00, 0x0400,
                new EnhancedConsoleMmioArea(machine)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#getModules()
	 */
	@Override
	public List<IModule> getModules() {
		return super.getModules();
	}
}
