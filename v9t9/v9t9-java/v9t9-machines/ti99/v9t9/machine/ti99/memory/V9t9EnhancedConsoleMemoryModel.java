package v9t9.machine.ti99.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.engine.files.directory.DiskDirectoryMapper;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.StoredMemoryEntryFactory;
import v9t9.machine.EmulatorMachinesData;


/**
 * Enhanced console memory model with a more sensible layout.
 * <p>
 * This has:
 * @author ejs
 */
public class V9t9EnhancedConsoleMemoryModel extends TI994AStandardConsoleMemoryModel {

	public V9t9EnhancedConsoleMemoryModel(IBaseMachine machine) {
		super(machine);
	}

	@Override
	protected void initSettings(ISettingsHandler settings) {
		//ConsoleRamArea.settingEnhRam.setBoolean(true);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {

		// enhanced model can only load FORTH for now
		IMemoryEntry entry;
		
		URL dataURL = EmulatorMachinesData.getDataURL("../../../build/forth");
		DataFiles.addSearchPath(settings, dataURL.getPath());
		
		loadEnhancedBankedConsoleRom(eventNotifier, "nforthA.rom", "nforthB.rom");
		loadConsoleGrom(eventNotifier, "nforth.grm");
		entry = loadModuleGrom(eventNotifier, "FORTH", "nforthg.bin");
		
		if (entry != null) {
			// the high-GROM code is copied into RAM here
			try {
	    		CPU.getEntryAt(0x6000).loadSymbols(
	    				new FileInputStream(DataFiles.resolveFile(settings, 
	    						((DiskMemoryEntry) entry).getSymbolFilepath())));
			} catch (IOException e) {
				
			}
		}
		
		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK1", new File("../../v9t9-c/tools/Forth/disk1"));
		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK2", new File("../../v9t9-c/tools/Forth/disk2"));
		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK3", new File("../../v9t9-c/tools/Forth/disk3"));
		
	}
	

    protected IMemoryEntry loadEnhancedBankedConsoleRom(IEventNotifier eventNotifier, String filename1, String filename2) {
    	// not toggled based on writes to the ROM, but MMIO
    	IMemoryEntry cpuRomEntry;
    	try {
    		MemoryEntryInfo info = MemoryEntryInfoBuilder
				.wordMemoryEntry()
				.withAddress(0)
				.withSize(0x4000)
				.withFilename(filename1)
				.withFilename2(filename2)
				.create("CPU ROM (enhanced)");
		
    		cpuRomEntry = StoredMemoryEntryFactory.getInstance().newMemoryEntry(info);	
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename1 + " or " + filename2, e);
    		return null;
    	}
    	cpuRomEntry.getArea().setLatency(0);
    	memory.addAndMap(cpuRomEntry);
    	return cpuRomEntry;
    }

    @Override
	protected void defineConsoleMemory(IBaseMachine machine) {
	    MemoryEntry entry = new MemoryEntry("Super 48K expansion RAM", CPU, 
	    		0x4000, 0xC000, new EnhancedRamArea(0, 0xC000));
	    entry.getArea().setLatency(0);
		memory.addAndMap(entry);
	}
	
	@Override
	protected void defineMmioMemory(IBaseMachine machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0xFC00, 0x0400,
                new V9t9EnhancedConsoleMmioArea(machine)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
}
