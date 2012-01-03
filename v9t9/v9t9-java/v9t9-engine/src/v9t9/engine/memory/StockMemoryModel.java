/**
 * 
 */
package v9t9.engine.memory;


import ejs.base.properties.IProperty;

import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;

/**
 * @author ejs
 *
 */
public class StockMemoryModel implements IMemoryModel {

	private Memory memory;
	private MemoryDomain CPU;

	public StockMemoryModel() {
		memory = new Memory();
		CPU = new MemoryDomain(IMemoryDomain.NAME_CPU);
		memory.addDomain(IMemoryDomain.NAME_CPU, CPU);
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
	public IMemoryDomain getConsole() {
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
	public IMemory getMemory() {
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

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getOptionalRomProperties()
	 */
	@Override
	public IProperty[] getOptionalRomProperties() {
		return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getRequiredRomProperties()
	 */
	@Override
	public IProperty[] getRequiredRomProperties() {
		return null;
	}
}
