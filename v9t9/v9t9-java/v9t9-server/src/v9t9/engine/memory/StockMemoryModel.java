/**
 * 
 */
package v9t9.engine.memory;

import java.util.Collections;
import java.util.List;

import v9t9.emulator.common.IBaseMachine;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.SoundMmio;
import v9t9.emulator.hardware.memory.mmio.SpeechMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.modules.IModule;

/**
 * @author ejs
 *
 */
public class StockMemoryModel implements MemoryModel {

	private Memory memory;
	private MemoryDomain CPU;

	public StockMemoryModel() {
		memory = new Memory(this);
		CPU = new MemoryDomain("Console");
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getConsole()
	 */
	public MemoryDomain getConsole() {
		return CPU;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getLatency(int)
	 */
	/**
	 * @param addr  
	 */
	public int getLatency(int addr) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getMemory()
	 */
	public Memory getMemory() {
		return memory;
	}

	public void initMemory(IBaseMachine machine) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#loadMemory(v9t9.emulator.clients.builtin.IEventNotifier)
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		
	}
	
	public GplMmio getGplMmio() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SoundMmio getSoundMmio() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SpeechMmio getSpeechMmio() {
		// TODO Auto-generated method stub
		return null;
	}
	public VdpMmio getVdpMmio() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getModules()
	 */
	public List<IModule> getModules() {
		return Collections.emptyList();
	}
}
