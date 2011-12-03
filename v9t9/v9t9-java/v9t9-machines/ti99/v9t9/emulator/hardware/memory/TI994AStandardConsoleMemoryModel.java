/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.emulator.hardware.memory;


import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.common.IBaseMachine;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.IMachine;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.memory.mmio.ConsoleGramWriteArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleGromReadArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleSoundArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleSpeechReadArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleSpeechWriteArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleVdpReadArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleVdpWriteArea;
import v9t9.engine.memory.MemoryEntry;

/**
 * The standard TI-99/4[A] console memory map.
 * @author ejs
 */
public class TI994AStandardConsoleMemoryModel extends BaseTI994AMemoryModel {
    static public final SettingProperty settingExpRam = new SettingProperty("MemoryExpansion32K", new Boolean(false));

    public TI994AStandardConsoleMemoryModel() {
    	super();
    }
    
    
    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#loadMemory()
     */
	@Override
    public void loadMemory(IEventNotifier eventNotifier) {
    	loadConsoleRom(eventNotifier, "994arom.bin");
    	loadConsoleGrom(eventNotifier, "994agrom.bin");    	
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#resetMemory()
     */
	@Override
    public void resetMemory() {
    	for (MemoryEntry entry : CPU.getMemoryEntries()) {
    		if (entry.addr == 0x4000 || entry.addr == 0x6000)
    			CPU.unmapEntry(entry);
    	}
    	for (MemoryEntry entry : GRAPHICS.getMemoryEntries()) {
    		if (entry.addr >= 0x6000)
    			GRAPHICS.unmapEntry(entry);
    	}
    }
    

    protected void initSettings() {
		WorkspaceSettings.CURRENT.register(ExpRamArea.settingExpRam);
		WorkspaceSettings.CURRENT.register(ConsoleRamArea.settingEnhRam);

		ExpRamArea.settingExpRam.setBoolean(true);
		ConsoleRamArea.settingEnhRam.setBoolean(false);
	}
 
	@Override
	protected void defineConsoleMemory(IBaseMachine machine) {
	    memory.addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
	            0x2000, new ExpRamArea(0x2000)));
	    memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
	            new ConsoleRamArea()));
	    memory.addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
	            0x6000, new ExpRamArea(0x6000)));
		
	}

   @Override
   protected void defineMmioMemory(IBaseMachine machine) {
        
        this.memory.addAndMap(new MemoryEntry("Sound MMIO", CPU, 0x8400, 0x0400,
                new ConsoleSoundArea(soundMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Read MMIO", CPU, 0x8800, 0x0400,
                new ConsoleVdpReadArea(((IMachine) machine).getVdp().getVdpMmio())));
        this.memory.addAndMap(new MemoryEntry("VDP Write MMIO", CPU, 0x8C00, 0x0400,
                new ConsoleVdpWriteArea(((IMachine) machine).getVdp().getVdpMmio())));
        this.memory.addAndMap(new MemoryEntry("Speech Read MMIO", CPU, 0x9000, 0x0400,
                new ConsoleSpeechReadArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("Speech Write MMIO", CPU, 0x9400, 0x0400,
                new ConsoleSpeechWriteArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("GROM Read MMIO", CPU, 0x9800, 0x0400,
                new ConsoleGromReadArea(gplMmio)));
        this.memory.addAndMap(new MemoryEntry("GRAM Write MMIO", CPU, 0x9C00, 0x0400,
                new ConsoleGramWriteArea(gplMmio)));
		
	}
  
}
