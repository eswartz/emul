/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.memory.ConsoleRamArea;
import v9t9.emulator.hardware.memory.ExpRamArea;
import v9t9.emulator.hardware.memory.StandardConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.SpeechMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.Client;
import v9t9.engine.memory.MemoryDomain;

public class TI994A extends Machine {

	public TI994A() {
		this(new StandardMachineModel());
	}
	
    public TI994A(MachineModel machineModel) {
        super(machineModel);
        getSettings().register(ExpRamArea.settingExpRam);
        getSettings().register(ConsoleRamArea.settingEnhRam);
    }
    
    @Override
	public void setClient(Client client) {
        super.setClient(client);
        
        getVdpMmio().setClient(client);
        getGplMmio().setClient(client);
        getSoundMmio().setClient(client);
        getSpeechMmio().setClient(client);
    }
    
    public v9t9.emulator.hardware.memory.mmio.SoundMmio getSoundMmio() {
        return ((StandardConsoleMemoryModel) memoryModel).soundMmio;
    }
    public VdpMmio getVdpMmio() {
        return getVdp().getVdpMmio();
    }
    public GplMmio getGplMmio() {
        return ((StandardConsoleMemoryModel) memoryModel).gplMmio;
    }
    public SpeechMmio getSpeechMmio() {
    	return ((StandardConsoleMemoryModel) memoryModel).speechMmio;
    }
    
 	public MemoryDomain getGplMemoryDomain() {
		return memory.getDomain("GRAPHICS");
	}
	public MemoryDomain getSpeechMemoryDomain() {
		return memory.getDomain("SPEECH");
	}
	public MemoryDomain getVdpMemoryDomain() {
		return memory.getDomain("VIDEO");
	}

}

