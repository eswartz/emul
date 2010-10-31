package v9t9.emulator.hardware.memory;

import java.io.*;

import v9t9.emulator.common.*;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper;
import v9t9.emulator.hardware.memory.mmio.*;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.*;


/**
 * MFP201 memory model
 * <p>
 * This has:
 * @author ejs
 */
public class MFP201MemoryModel implements MemoryModel {
	/** CPU ROM/RAM */
    protected MemoryDomain console;
    /** video memory */
    protected MemoryDomain video;
	protected Memory memory;
	private VdpMmio vdpMmio;
	private SoundMmio soundMmio;
    
	public MFP201MemoryModel() {
		super();
	}
	 
    public Memory createMemory() {
    	this.memory = new Memory(this);
    	
        console = new MemoryDomain("Console", 1);
        memory.addDomain("CPU", console);
        
        video = new MemoryDomain("Video", 1);
        memory.addDomain("VIDEO", video);
        
        return memory;
        
    }
    
    public void initMemory(Machine machine) {
        defineConsoleMemory();
     
    }

    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#getConsole()
     */
    @Override
    public MemoryDomain getConsole() {
    	return console;
    }
    
    protected void reportLoadError(IEventNotifier eventNotifier, String file, @SuppressWarnings("unused") IOException e) {
		eventNotifier.notifyEvent(this, IEventNotifier.Level.ERROR, 
				"Failed to find image '" + file +"' which is needed to start"); 

    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#resetMemory()
     */
    @Override
    public void resetMemory() {
    	for (MemoryEntry entry : console.getMemoryEntries()) {
    		if (entry.addr == 0xf000)
    			console.unmapEntry(entry);
    	}
    }
    


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {

		DataFiles.addSearchPath("../../system/mfp201");
		loadConsoleRom(eventNotifier, "rom.rom");
		
		DiskDirectoryMapper.INSTANCE.setDiskPath("DSK1", new File("../../system/mfp201"));
		
	}
	

    protected DiskMemoryEntry loadConsoleRom(IEventNotifier eventNotifier, String filename) {
    	DiskMemoryEntry cpuRomEntry;
    	try {
			cpuRomEntry = DiskMemoryEntry.newWordMemoryFromFile(
	    			0xf000, 0x1000, "CPU ROM",
	        		console,
	                filename, 0x0, false);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, filename, e);
    		return null;
    	}
    	cpuRomEntry.getArea().setLatency(0);
		memory.addAndMap(cpuRomEntry);
		return cpuRomEntry;
    }

	protected void defineConsoleMemory() {
	    MemoryEntry entry = new MemoryEntry("64K RAM", console, 
	    		0x0000, 0x10000, new EnhancedRamArea(0, 0x10000));
	    entry.getArea().setLatency(0);
		memory.addAndMap(entry);
	}
	
	protected void defineMmioMemory(Machine machine) {
		vdpMmio = machine.getVdp().getVdpMmio();
		//soundMmio = machine.getSound(). getSoundMmio();
		
		this.memory.addAndMap(new MemoryEntry("MMIO", console, 0xFC00, 0x0400,
                new MFP201ConsoleMmioArea(machine)));
	}

	/**
	 * @return
	 */
	public VdpMmio getVdpMmio() {
		return vdpMmio;
	}

	/**
	 * @return
	 */
	public SoundMmio getSoundMmio() {
		return soundMmio;
	}
}
