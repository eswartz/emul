/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.engine.memory.MemoryDomain;

public class BaseInstructionWorkBlock  {
	/** the CPU */
	public CpuState cpu;
    /** our CPU memory */
    public MemoryDomain domain;
    /** the instruction (in) */
    public RawInstruction inst;	
    /** values (in: original, out: changed, if needed) */
    public short pc;
    /** status word (in/out) */
    public short st;
    /** cycle count */
    public int cycles;
    
    public BaseInstructionWorkBlock(CpuState cpu) {
    	this.cpu = cpu;
    	this.domain = cpu.getConsole();
	}
    
    public void copyTo(BaseInstructionWorkBlock copy) {
    	copy.cpu = cpu;
    	copy.domain = domain;
    	copy.inst = inst;
    	copy.pc = pc;
    	copy.st = st;
    }
}