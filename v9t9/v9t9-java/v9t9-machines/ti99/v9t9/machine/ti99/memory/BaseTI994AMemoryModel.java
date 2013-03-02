/*
  BaseTI994AMemoryModel.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.memory;

import java.io.FileNotFoundException;
import java.io.IOException;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.Settings;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntryFactory;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.SoundMmio;
import v9t9.engine.memory.SpeechMmio;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.engine.memory.Vdp9918AMmio;
import v9t9.engine.memory.Vdp9938Mmio;
import v9t9.engine.memory.VdpMmio;
import v9t9.engine.video.v9938.VdpV9938;

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
	public VdpMmio vdpMmio;
	protected ISettingsHandler settings;
	protected final IMachine machine;

	/**
	 * @param machine 
	 * 
	 */
	public BaseTI994AMemoryModel(IMachine machine) {
		super();
		this.machine = machine;
		settings = Settings.getSettings(machine);
		initSettings(settings);
	}

	abstract protected void initSettings(ISettingsHandler settings);

    public IMemory getMemory() {
    	if (memory == null) {
	    	this.memory = new Memory();
	    	memory.setModel(this);
	    	memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, machine.getRomPathFileLocator()));
	    	
	        CPU = new MemoryDomain(IMemoryDomain.NAME_CPU, "Console", 4);
	        GRAPHICS = new MemoryDomain(IMemoryDomain.NAME_GRAPHICS, "GROM/GRAM");
	        VIDEO = new MemoryDomain(IMemoryDomain.NAME_VIDEO, "VDP");
	        SPEECH = new MemoryDomain(IMemoryDomain.NAME_SPEECH, "Speech");
	        
	        memory.addDomain(IMemoryDomain.NAME_CPU, CPU);
	        memory.addDomain(IMemoryDomain.NAME_GRAPHICS, GRAPHICS);
	        memory.addDomain(IMemoryDomain.NAME_VIDEO, VIDEO);
	        memory.addDomain(IMemoryDomain.NAME_SPEECH, SPEECH);
    	}
        return memory;
        
    }
    

    @Override
	public void initMemory(IBaseMachine machine) {
        defineConsoleMemory(machine);
     
        soundMmio = new SoundMmio(((IMachine) machine).getSound());
        gplMmio = new GplMmio(machine, GRAPHICS);
        speechMmio = new SpeechMmio(((IMachine) machine).getSpeech());
        
        ISettingsHandler settings = Settings.getSettings(machine);
        IVdpChip vdp = ((IMachine) machine).getVdp();
        if (vdp instanceof VdpV9938)
        	vdpMmio = new Vdp9938Mmio(settings, machine.getMemory(), (VdpV9938) vdp, 0x20000);
        else
        	vdpMmio = new Vdp9918AMmio(settings, machine.getMemory(), vdp, 0x4000);

        defineMmioMemory(machine);
    }

	protected void reportLoadError(IEventNotifier eventNotifier, String file, IOException e) {
		eventNotifier.notifyEvent(this, IEventNotifier.Level.ERROR, 
				e instanceof FileNotFoundException ?
				"Failed to find image '" + file +"' which is needed to start" : e.getMessage()); 
	
	}

	protected IMemoryEntry loadMemory(IEventNotifier eventNotifier, MemoryEntryInfo info) {
		IMemoryEntry entry;
		try {
			entry = memory.getMemoryEntryFactory().newMemoryEntry(info);
		} catch (IOException e) {
			reportLoadError(eventNotifier, info.getResolvedFilename(Settings.getSettings(machine)), e);
			return null;
		}
		
		IMemoryDomain domain = info.getDomain(memory);
		IMemoryEntry old = domain.getEntryAt(info.getAddress());
		if (old != null)
			domain.unmapEntry(old);
		
		memory.addAndMap(entry);
		return entry;
	}

	protected IMemoryEntry loadConsoleRom(IEventNotifier eventNotifier, String filename, String md5) {
		MemoryEntryInfo info = MemoryEntryInfoBuilder
			.standardConsoleRom(filename)
			.withFileMD5(md5)
			.create("CPU ROM");

		return loadMemory(eventNotifier, info);
	}
	

	protected IMemoryEntry loadConsoleGrom(IEventNotifier eventNotifier, String filename, String md5) {
		MemoryEntryInfo info = MemoryEntryInfoBuilder
			.standardConsoleGrom(filename)
			.withFileMD5(md5)
			.create("CPU GROM");

		return loadMemory(eventNotifier, info);
	}

	protected IMemoryEntry loadModuleGrom(IEventNotifier eventNotifier, String name, String filename) {
		MemoryEntryInfo info = MemoryEntryInfoBuilder
			.standardModuleGrom(filename)
			.create(name);

		return loadMemory(eventNotifier, info);
	}

	public IMemoryDomain getConsole() {
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