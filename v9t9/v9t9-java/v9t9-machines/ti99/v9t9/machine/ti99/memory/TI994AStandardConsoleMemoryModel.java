/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.machine.ti99.memory;

import java.net.URL;

import ejs.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.actors.SoundMmioDataDemoActor;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.speech.SpeechTMS5220;
import v9t9.machine.ti99.dsr.pcode.PCodeDsr;
import v9t9.machine.ti99.dsr.realdisk.RealDiskImageDsr;
import v9t9.machine.ti99.memory.mmio.ConsoleGramWriteArea;
import v9t9.machine.ti99.memory.mmio.ConsoleGromReadArea;
import v9t9.machine.ti99.memory.mmio.ConsoleSoundArea;
import v9t9.machine.ti99.memory.mmio.ConsoleSpeechReadArea;
import v9t9.machine.ti99.memory.mmio.ConsoleSpeechWriteArea;
import v9t9.machine.ti99.memory.mmio.ConsoleVdpReadArea;
import v9t9.machine.ti99.memory.mmio.ConsoleVdpWriteArea;

/**
 * The standard TI-99/4[A] console memory map.
 * @author ejs
 */
public class TI994AStandardConsoleMemoryModel extends BaseTI994AMemoryModel {
    static public final SettingSchema settingRomFileName = new SettingSchema(
    		ISettingsHandler.WORKSPACE,
    		"RomFileName", "994arom.bin");
    static public final SettingSchema settingGromFileName = new SettingSchema(
    		ISettingsHandler.WORKSPACE,
    		"GromFileName", "994agrom.bin");

	static public final SettingSchema settingExpRam = new SettingSchema(
    		ISettingsHandler.WORKSPACE,
    		"MemoryExpansion32K", new Boolean(false));

	static protected final MemoryEntryInfo cpuRomInfo = MemoryEntryInfoBuilder
		.standardConsoleRom(null)
		.withFilenameProperty(settingRomFileName)
		.withFileMD5("6CC4BC2B6B3B0C33698E6A03759A4CAB")
		.withDescription("TI-99/4A Console ROM")
		.create("CPU ROM");
	
	static protected final MemoryEntryInfo cpuGromInfo = MemoryEntryInfoBuilder
		.standardConsoleGrom(null)
		.withFilenameProperty(settingGromFileName)
		.withFileMD5("ED8FF714542BA850BDEC686840A79217")
		.withDescription("TI-99/4A Console GROM")
		.create("CPU GROM");
	
    public TI994AStandardConsoleMemoryModel(IMachine machine) {
    	super(machine);
    }
    
    
    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#loadMemory()
     */
	@Override
    public void loadMemory(IEventNotifier eventNotifier) {
    	loadMemory(eventNotifier, cpuRomInfo); 
    	loadMemory(eventNotifier, cpuGromInfo); 
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.memory.MemoryModel#resetMemory()
     */
	@Override
    public void resetMemory() {
    	for (IMemoryEntry entry : CPU.getMemoryEntries()) {
    		if (entry.getAddr() == 0x4000 || entry.getAddr() == 0x6000)
    			CPU.unmapEntry(entry);
    	}
    	for (IMemoryEntry entry : GRAPHICS.getMemoryEntries()) {
    		if (entry.getAddr() >= 0x6000)
    			GRAPHICS.unmapEntry(entry);
    	}
    }
    

    protected void initSettings(ISettingsHandler settings) {
		settings.get(ExpRamArea.settingExpRam).setBoolean(true);
		settings.get(ConsoleRamArea.settingEnhRam).setBoolean(false);
		
		IProperty demoPath = settings.get(IDemoManager.settingBootDemosPath); 
		if (demoPath.getList().isEmpty()) {
    		try {
				URL demosUrl = new URL(machine.getModel().getDataURL(), "demos/");
				demoPath.getList().add(new URL(demosUrl, "v9t9j").toURI().toString());
				demoPath.getList().add(new URL(demosUrl, "tiemul6").toURI().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
	}
 
	@Override
	protected void defineConsoleMemory(IBaseMachine machine) {
		ISettingsHandler settings = Settings.getSettings(machine);
		memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
				new ConsoleRamArea(settings)));
	    MemoryEntry lowRam = new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
	            0x2000, new ExpRamArea(settings, 0x2000));
	    lowRam.setVolatile(false);
		memory.addAndMap(lowRam);
	    MemoryEntry highRam = new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
	            0x6000, new ExpRamArea(settings, 0x6000));
	    highRam.setVolatile(false);
		memory.addAndMap(highRam);
		
	}

   @Override
   protected void defineMmioMemory(IBaseMachine machine) {
        
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
		
		((IMachine) machine).getDemoManager().registerActorProvider(new SoundMmioDataDemoActor.Provider(0x8400));
		((IMachine) machine).getDemoManager().registerActorProvider(new SoundMmioDataDemoActor.ReverseProvider(0x8400));

	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getRequiredRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getRequiredRomMemoryEntries() {
		return new MemoryEntryInfo[] { 
				cpuRomInfo,
				cpuGromInfo
				};
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getOptionalRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getOptionalRomMemoryEntries() {
		return new MemoryEntryInfo[] { 
				SpeechTMS5220.speechRomInfo,
				RealDiskImageDsr.dsrRomInfo,
				PCodeDsr.pcodeDsrRomBankAMemoryEntryInfo,
				PCodeDsr.pcodeDsrRomBankBMemoryEntryInfo,
				PCodeDsr.pcodeGromMemoryEntryInfo,
		};
	}

}
