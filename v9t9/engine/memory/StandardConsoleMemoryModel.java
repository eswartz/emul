/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.memory;

import v9t9.emulator.hardware.memory.ConsoleGramWriteArea;
import v9t9.emulator.hardware.memory.ConsoleGromReadArea;
import v9t9.emulator.hardware.memory.ConsoleSoundArea;
import v9t9.emulator.hardware.memory.ConsoleSpeechReadArea;
import v9t9.emulator.hardware.memory.ConsoleSpeechWriteArea;
import v9t9.emulator.hardware.memory.ConsoleVdpReadArea;
import v9t9.emulator.hardware.memory.ConsoleVdpWriteArea;
import v9t9.emulator.hardware.memory.ExpRamArea;
import v9t9.emulator.hardware.memory.StdConsoleRamArea;
import v9t9.emulator.hardware.memory.VdpRamArea;
import v9t9.emulator.runtime.Sound;
import v9t9.emulator.runtime.Speech;
import v9t9.emulator.runtime.Vdp;
import v9t9.engine.Client;
import v9t9.engine.settings.Setting;

/**
 * The standard TI-99/4[A] console memory map.
 * @author ejs
 */
public class StandardConsoleMemoryModel {
    /* CPU ROM/RAM */
    public MemoryDomain CPU;

    /* GPL ROM/RAM */
    public MemoryDomain GRAPHICS;

    /* VDP RAM */
    public MemoryDomain VIDEO;

    /* Speech ROM */
    public MemoryDomain SPEECH;

    
    public boolean bHasExpRam; /* is 32k expansion RAM enabled? */

    public boolean bHasEnhRam; /* is enhanced this RAM enabled? */

    static public final String sExpRam = "MemoryExpansion32K";
    static public final String sEnhRam = "ExtraConsoleRAM";
    static public final Setting settingExpRam = new Setting(sExpRam, new Boolean(false));
    static public final Setting settingEnhRam = new Setting(sEnhRam, new Boolean(false));

    public Vdp vdpMmio;
    
    public Sound soundMmio;
    
    public Speech speechMmio;
    public Gpl gplMmio;

    private Client client;

	private Memory memory;
    
    public StandardConsoleMemoryModel(Memory memory) {
    	this.memory = memory;
        CPU = new MemoryDomain();
        GRAPHICS = new MemoryDomain();
        VIDEO = new MemoryDomain();
        SPEECH = new MemoryDomain();

        memory.addDomain(CPU);
        memory.addDomain(VIDEO);
        memory.addDomain(GRAPHICS);
        memory.addDomain(SPEECH);
        
        memory.addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
                0x2000, new ExpRamArea(0x2000)));
        memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
                new StdConsoleRamArea()));
        memory.addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
                0x6000, new ExpRamArea(0x6000)));
     
        memory.addAndMap(new MemoryEntry("VDP RAM", VIDEO, 0x0000, 0x4000, 
                new VdpRamArea()));

    }

    public void connectClient(Client client) {
        this.client = client;

        vdpMmio = new v9t9.emulator.runtime.Vdp(VIDEO, this.client);
        gplMmio = new Gpl(GRAPHICS);
        soundMmio = new v9t9.emulator.runtime.Sound(this.client);
        speechMmio = new Speech(this.client);

        vdpMmio.setClient(client);
        soundMmio.setClient(client);
        speechMmio.setClient(client);

        this.memory.addAndMap(new MemoryEntry("Sound MMIO", CPU, 0x8400, 0x0400,
                new ConsoleSoundArea(soundMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Read MMIO", CPU, 0x8800, 0x0400,
                new ConsoleVdpReadArea(vdpMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Write MMIO", CPU, 0x8C00, 0x0400,
                new ConsoleVdpWriteArea(vdpMmio)));
        this.memory.addAndMap(new MemoryEntry("Speech Read MMIO", CPU, 0x9000, 0x0400,
                new ConsoleSpeechReadArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("Speech Write MMIO", CPU, 0x9400, 0x0400,
                new ConsoleSpeechWriteArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("GROM Read MMIO", CPU, 0x9800, 0x0400,
                new ConsoleGromReadArea(gplMmio)));
        this.memory.addAndMap(new MemoryEntry("GRAM Write MMIO", CPU, 0x9C00, 0x0400,
                new ConsoleGramWriteArea(gplMmio)));
        
        this.memory.addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
                0x2000, new ExpRamArea(0x2000)));
        this.memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
                new StdConsoleRamArea()));
        this.memory.addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
                0x6000, new ExpRamArea(0x6000)));
     
        this.memory.addAndMap(new MemoryEntry("VDP RAM", VIDEO, 0x0000, 0x4000, 
                new VdpRamArea()));

    }
 

}
