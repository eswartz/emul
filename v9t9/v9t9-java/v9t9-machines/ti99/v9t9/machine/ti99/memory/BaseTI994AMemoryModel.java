/**
 * 
 */
package v9t9.machine.ti99.memory;

import java.io.IOException;

import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.Memory;
import v9t9.common.memory.MemoryDomain;
import v9t9.engine.machine.IMachine;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.SoundMmio;
import v9t9.engine.memory.SpeechMmio;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.engine.memory.VdpMmio;

/**
 * @author ejs
 *
 */
public abstract class BaseTI994AMemoryModel implements TIMemoryModel {

	protected MemoryDomain CPU;

	protected abstract void defineMmioMemory(IBaseMachine machine);

	protected abstract void defineConsoleMemory(IBaseMachine machine);

	protected MemoryDomain GRAPHICS;
	protected MemoryDomain VIDEO;
	protected MemoryDomain SPEECH;
	public SoundMmio soundMmio;
	public SpeechMmio speechMmio;
	public GplMmio gplMmio;
	protected Memory memory;
	protected VdpMmio vdpMmio;

	/**
	 * 
	 */
	public BaseTI994AMemoryModel() {
		super();
		initSettings();
	}

	abstract protected void initSettings();

    public Memory getMemory() {
    	if (memory == null) {
	    	this.memory = new Memory(this);
	    	
	        CPU = new MemoryDomain("Console", 4);
	        GRAPHICS = new MemoryDomain("GROM/GRAM");
	        VIDEO = new MemoryDomain("VDP");
	        SPEECH = new MemoryDomain("Speech");
	        
	        memory.addDomain(MemoryDomain.NAME_CPU, CPU);
	        memory.addDomain(MemoryDomain.NAME_GRAPHICS, GRAPHICS);
	        memory.addDomain(MemoryDomain.NAME_VIDEO, VIDEO);
	        memory.addDomain(MemoryDomain.NAME_SPEECH, SPEECH);
    	}
        return memory;
        
    }
    

    @Override
	public void initMemory(IBaseMachine machine) {
        defineConsoleMemory(machine);
     
        soundMmio = new SoundMmio(((IMachine) machine).getSound());
        gplMmio = new GplMmio(GRAPHICS);
        speechMmio = new SpeechMmio(((IMachine) machine).getSpeech());
        
        vdpMmio = ((IMachine) machine).getVdp().getVdpMmio();
        defineMmioMemory(machine);
    }
    
	protected void reportLoadError(IEventNotifier eventNotifier, String file, IOException e) {
		eventNotifier.notifyEvent(this, IEventNotifier.Level.ERROR, 
				"Failed to find image '" + file +"' which is needed to start"); 
	
	}

	protected DiskMemoryEntry loadConsoleRom(IEventNotifier eventNotifier, String filename) {
		DiskMemoryEntry cpuRomEntry;
		try {
			cpuRomEntry = DiskMemoryEntry.newWordMemoryFromFile(
	    			0x0, 0x2000, "CPU ROM",
	        		CPU,
	                filename, 0x0, false);
		} catch (IOException e) {
			reportLoadError(eventNotifier, filename, e);
			return null;
		}
		cpuRomEntry.getArea().setLatency(0);
		memory.addAndMap(cpuRomEntry);
		return cpuRomEntry;
	}

	protected DiskMemoryEntry loadConsoleGrom(IEventNotifier eventNotifier, String filename) {
		DiskMemoryEntry entry;
		try {
			entry = DiskMemoryEntry.newByteMemoryFromFile(
	    			0x0, 0x6000, "CPU GROM", 
	    			GRAPHICS,
	    			filename, 0x0, false);
		} catch (IOException e) {
			reportLoadError(eventNotifier, filename, e);
			return null;
		}
		memory.addAndMap(entry);
		return entry;
	}

	protected DiskMemoryEntry loadModuleGrom(IEventNotifier eventNotifier, String name, String filename) {
		DiskMemoryEntry entry;
		try {
			entry = DiskMemoryEntry.newByteMemoryFromFile(
	    			0x6000, 0, name, 
	    			GRAPHICS,
	    			filename, 0x0, false);
		} catch (IOException e) {
			reportLoadError(eventNotifier, filename, e);
			return null;
		}
		memory.addAndMap(entry);
		return entry;
	}

	public MemoryDomain getConsole() {
		return CPU;
	}

	public GplMmio getGplMmio() {
		return gplMmio;
	}

	public SoundMmio getSoundMmio() {
		return soundMmio;
	}

	public SpeechMmio getSpeechMmio() {
		return speechMmio;
	}

	public VdpMmio getVdpMmio() {
		return vdpMmio;
	}

}