package v9t9.engine.memory;

import java.io.PrintWriter;

import v9t9.emulator.runtime.Logging;
import v9t9.emulator.runtime.cpu.Executor;

/**
 * This is a standard TI-99/4A style banked memory entry.
 * @author ejs
 *
 */
public class StdMultiBankedMemoryEntry extends
		MultiBankedMemoryEntry {
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
	public StdMultiBankedMemoryEntry(Memory memory, String name,
			MemoryEntry[] banks) {
		super(memory, name, banks);
	}

	@Override
	public void writeByte(int addr, byte val) {
		int bank = (addr & 2) >> 1;
		if (selectBank(bank)) {
			
			PrintWriter log = Logging.getLog(Executor.settingDumpFullInstructions);
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
			PrintWriter log = Logging.getLog(Executor.settingDumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeWord(addr, val);
	}
}