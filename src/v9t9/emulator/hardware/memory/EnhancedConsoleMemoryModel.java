/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.TI994A;
import v9t9.engine.memory.MemoryEntry;


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
	    		0x4000, 0xC000, new ExpRamArea(0, 0xC000));
	    entry.area.setLatency(0);
		memory.addAndMap(entry);
	}
	
	protected void defineMmioMemory(TI994A machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0xFC00, 0x0400,
                new EnhanchedConsoleMmioArea(machine)));
		
		/*
		// TODO: remove
        this.memory.addAndMap(new MemoryEntry("Sound MMIO", CPU, 0x8400, 0x0400,
                new ConsoleSoundArea(soundMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Read MMIO", CPU, 0x8800, 0x0400,
                new ConsoleVdpReadArea(machine.getVdpMmio())));
        this.memory.addAndMap(new MemoryEntry("VDP Write MMIO", CPU, 0x8C00, 0x0400,
                new ConsoleVdpWriteArea(machine.getVdpMmio())));
        //this.memory.addAndMap(new MemoryEntry("Speech Read MMIO", CPU, 0x9000, 0x0400,
                //new ConsoleSpeechReadArea(speechMmio)));
        //this.memory.addAndMap(new MemoryEntry("Speech Write MMIO", CPU, 0x9400, 0x0400,
                //new ConsoleSpeechWriteArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("GROM Read MMIO", CPU, 0x9800, 0x0400,
                new ConsoleGromReadArea(gplMmio)));
        this.memory.addAndMap(new MemoryEntry("GRAM Write MMIO", CPU, 0x9C00, 0x0400,
                new ConsoleGramWriteArea(gplMmio)));
		*/
	}
}
