package v9t9.engine.memory;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;

/**
 * This is a standard TI-99/4A style banked memory entry.
 * @author ejs
 *
 */
public class StdMultiBankedMemoryEntry extends
		MultiBankedMemoryEntry {
	private IProperty dumpFullInstructions;

	/**
	 * Only to be used when reconstructing 
	 */
	public StdMultiBankedMemoryEntry() {
	}
	/**
	 * @param memory
	 * @param name
	 * @param banks
	 */
	public StdMultiBankedMemoryEntry(ISettingsHandler settings, IMemory memory, String name,
			IMemoryEntry[] banks) {
		super(settings, memory, name, banks);
		dumpFullInstructions = settings.get(ICpu.settingDumpFullInstructions);
	}

	@Override
	public boolean isStatic() {
		return false;
	}
	
	@Override
	public void writeByte(int addr, byte val) {
		int bank = (addr & 2) >> 1;
		if (selectBank(bank)) {
			
			PrintWriter log = Logging.getLog(dumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeByte(addr, val);
	}

	@Override
	public void writeWord(int addr, short val) {
		int bank = (addr & 2) >> 1;
		if (selectBank(bank)) {
			PrintWriter log = Logging.getLog(dumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeWord(addr, val);
	}
}