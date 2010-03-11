/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.emulator.hardware.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import v9t9.emulator.IEventNotifier;
import v9t9.emulator.Machine;
import v9t9.emulator.hardware.TI994A;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
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

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {

		// enhanced model can only load FORTH for now
		DiskMemoryEntry entry;
		
		loadEnhancedBankedConsoleRom(eventNotifier, "nforthA.rom", "nforthB.rom");
		loadConsoleGrom(eventNotifier, "nforth.grm");
		entry = loadModuleGrom(eventNotifier, "FORTH", "nforthg.bin");
		
		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK1", new File("../../tools/Forth"));
		
		// the high-GROM code is copied into RAM here
		try {
    		CPU.getEntryAt(0x6000).loadSymbols(
    				new FileInputStream(DataFiles.resolveFile(entry.getSymbolFilepath())));
		} catch (IOException e) {
			
		}
	}
	

    protected BankedMemoryEntry loadEnhancedBankedConsoleRom(IEventNotifier eventNotifier, String filename1, String filename2) {
    	// not toggled based on writes to the ROM, but MMIO
    	BankedMemoryEntry cpuRomEntry;
    	try {
			cpuRomEntry = DiskMemoryEntry.newBankedWordMemoryFromFile(
	    			0x0000,
	    			0x4000,
	    			memory,
	    			"CPU ROM (enhanced)", CPU,
	    			filename1, 0x0, filename2, 0x0);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename1 + " or " + filename2, e);
    		return null;
    	}
    	cpuRomEntry.getArea().setLatency(0);
    	memory.addAndMap(cpuRomEntry);
    	return cpuRomEntry;
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
}
