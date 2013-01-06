package v9t9.engine.memory;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;

/**
 * This is a banked memory entry as supported by the PCB boards. 
 * @author ejs
 *
 */
public class PCBBankedMemoryEntry extends
		MultiBankedMemoryEntry {
	private IProperty dumpFullInstructions;

	/**
	 * Only to be used when reconstructing 
	 */
	public PCBBankedMemoryEntry() {
	}
	/**
	 * @param memory
	 * @param name
	 * @param banks
	 */
	public PCBBankedMemoryEntry(ISettingsHandler settings, IMemory memory, String name,
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
		int bank = (addr & 0xff) >> 1;
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
		int bank = (addr & 0xff) >> 1;
		if (selectBank(bank)) {
			PrintWriter log = Logging.getLog(dumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeWord(addr, val);
	}
}