/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.engine.memory.MemoryDomain;

public final class InstructionWorkBlock  {
    /** our CPU memory */
    public MemoryDomain domain;
    /** the instruction (in) */
    public Instruction9900 inst;	
    /** EAs for operands 1 and 2 */
    public short ea1, ea2;
    /** values for operands 1 and 2 (in: EAs or values, out: value)
    for MPY/DIV, val3 holds lo reg */
    public short val1, val2, val3;	
    /** values (in: original, out: changed, if needed) */
    public short pc, wp;
    /** status word (in/out) */
    public Status status;
    /** cycle count */
    public int cycles;
    
    public InstructionWorkBlock(Status status) {
    	this.status = status;
	}
    
    public void copyTo(InstructionWorkBlock copy) {
    	copy.domain = domain;
    	copy.inst = inst;
    	copy.ea1 = ea1;
    	copy.ea2 = ea2;
    	copy.val1 = val1;
    	copy.val2 = val2;
    	copy.val3 = val2;
    	copy.pc = pc;
    	copy.wp = wp;
    	status.copyTo(copy.status);
    }
}