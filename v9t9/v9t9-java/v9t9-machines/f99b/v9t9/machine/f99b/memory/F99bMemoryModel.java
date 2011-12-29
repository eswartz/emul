package v9t9.machine.f99b.memory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.StoredMemoryEntryFactory;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.memory.BaseTI994AMemoryModel;


/**
 * F99b console memory model.
 * @author ejs
 */
public class F99bMemoryModel extends BaseTI994AMemoryModel {

	private MemoryEntry consoleEntry;

	public F99bMemoryModel(IMachine machine) {
		super(machine);
	}

	@Override
	protected void initSettings(ISettingsHandler settings) {
		URL dataURL = EmulatorMachinesData.getDataURL("../../../build/forth99");
		DataFiles.addSearchPath(settings, dataURL.getPath());
	}


	protected void defineConsoleMemory(IBaseMachine machine) {
		consoleEntry = new MemoryEntry("64K RAM", CPU, 
				0x0400, 0xFC00, new EnhancedRamByteArea(0, 0xFC00));
		memory.addAndMap(consoleEntry);
	}
	
	protected void defineMmioMemory(IBaseMachine machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0x0000, 0x0400,
                new F99bConsoleMmioArea((IMachine) machine)));
	}
	
	private static MemoryEntryInfo f99bRomMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withOffset(0x400)
		.withAddress(0x400)
		.create("CPU ROM");
	
	private static MemoryEntryInfo f99bGramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withDomain(IMemoryDomain.NAME_GRAPHICS)
		.withAddress(0x4000)
		.withSize(0x4000)
		.create("16K GRAM Dictionary");
	
	private static MemoryEntryInfo f99bDiskGramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withFilename("f99bgram.bin")
		.withDomain(IMemoryDomain.NAME_GRAPHICS)
		.withAddress(0x8000)
		.withSize(0x8000)
		.setStored(true)
		.create("GRAM");

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		IMemoryEntry cpuRomEntry;
		String filename = "f99brom.bin";
    	try {
			cpuRomEntry = StoredMemoryEntryFactory.getInstance().newMemoryEntry(
					f99bRomMemoryEntryInfo);
			cpuRomEntry.load();
			cpuRomEntry.copySymbols(CPU);
			
			// shrink RAM accordingly
			int st = cpuRomEntry.getAddr() + 0x400 * ((cpuRomEntry.getSize() + 0x3ff) / 0x400);
			int sz = 0x10000 - st;
			memory.removeAndUnmap(consoleEntry);
			consoleEntry = new MemoryEntry("64K RAM", CPU, 
					st, sz, new EnhancedRamByteArea(0, sz));
			memory.addAndMap(consoleEntry);
			
			memory.addAndMap(cpuRomEntry);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename, e);
    	}
    	
    	// see if we need to merge the GROM dictionary
    	String FORTH_GROM = "forth99.grm";
    	String GROM_DICT = "f99bgromdict.bin";
    	File dictFile = null;

    	File gromFile = DataFiles.resolveFile(settings, FORTH_GROM);
    	dictFile = DataFiles.resolveFile(settings, GROM_DICT);
    	if (gromFile.exists() && dictFile.exists() && dictFile.lastModified() > gromFile.lastModified()) {

    		byte[] grom = new byte[(int) gromFile.length()];
    		
			int gromDictSize = (int) dictFile.length();

			try {
				DataFiles.readMemoryImage(settings, FORTH_GROM, 0, grom.length, grom);
				int gromDictBase = (grom[2] << 8) | (grom[3] & 0xff);
				
				if (gromDictSize + gromDictBase > 16 * 1024) {
					reportLoadError(eventNotifier, GROM_DICT, new IOException("GROM dictionary too big!  GROM plus dictionary maxes out at 16k."));
				}
				
				byte[] gromDict = new byte[gromDictSize];
				DataFiles.readMemoryImage(settings, GROM_DICT, 0, gromDictSize, gromDict);
				
				for (int i = 0; i < gromDictSize; i++) {
					grom[i + gromDictBase] = gromDict[i];
				}
				
				int end = gromDictSize + gromDictBase;
				grom[4] = (byte) (end >> 8);
				grom[5] = (byte) (end & 0xff);
				
				DataFiles.writeMemoryImage(settings, gromFile.getAbsolutePath(), grom.length, grom);
				
				eventNotifier.notifyEvent(null, Level.INFO, "Merged dictionary into GROM, changed " + gromFile);
			} catch (IOException e) {
				reportLoadError(eventNotifier, FORTH_GROM, 
						(IOException) new IOException("Failed to merge dictionary into GROM").initCause(e));
				
			}
    	}
    	
    	
    	// GROM consists of ROM for 16k
		loadConsoleGrom(eventNotifier, FORTH_GROM);
		
		// then 16k of volatile GRAM for new dictionary
		IMemoryEntry gramDictEntry;
		try {
			gramDictEntry = StoredMemoryEntryFactory.getInstance().newMemoryEntry(
					f99bGramMemoryEntryInfo);
			gramDictEntry.getArea().setLatency(0);
			memory.addAndMap(gramDictEntry);
		} catch (IOException e1) {
			// should not happen
			reportLoadError(eventNotifier, f99bGramMemoryEntryInfo.getFilename(), e1);
		}
		
		// then 32k of GRAM storage
		try {
			IMemoryEntry entry = StoredMemoryEntryFactory.getInstance().newMemoryEntry(
					f99bDiskGramMemoryEntryInfo);
			memory.addAndMap(entry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, f99bDiskGramMemoryEntryInfo.getFilename(), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
}
