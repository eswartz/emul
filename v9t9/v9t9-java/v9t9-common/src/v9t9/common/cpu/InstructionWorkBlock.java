/**
 * 
 */
package v9t9.common.cpu;

import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.MemoryDomain;

/**
 * This block contains variables being modified
 * during execution of an instruction.  Its fields
 * are public for efficiency.
 * @author ejs
 *
 */
public class InstructionWorkBlock  {
	/** the CPU */
	public ICpuState cpu;
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
    
    public InstructionWorkBlock(ICpuState cpu) {
    	this.cpu = cpu;
    	this.domain = cpu.getConsole();
	}
    
    public void copyTo(InstructionWorkBlock copy) {
    	copy.cpu = cpu;
    	copy.domain = domain;
    	copy.inst = inst;
    	copy.pc = pc;
    	copy.st = st;
    	copy.cycles = cycles;
    }
}