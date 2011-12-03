/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.emulator.runtime.cpu.CpuState9900;

public final class InstructionWorkBlock9900 extends BaseInstructionWorkBlock {
    /** EAs for operands 1 and 2 */
    public short ea1, ea2, ea3;
    /** values for operands 1 and 2 (in: EAs or values, out: value)
    for MPY/DIV, val3 holds lo reg */
    public short val1, val2, val3;	
    public short wp;
    
    public InstructionWorkBlock9900(CpuState cpu) {
    	super(cpu);
    	if (cpu instanceof CpuState9900)
    		this.wp = ((CpuState9900) cpu).getWP();
	}
    
    public void copyTo(InstructionWorkBlock9900 copy) {
    	super.copyTo(copy);
    	copy.ea1 = ea1;
    	copy.ea2 = ea2;
    	copy.ea3 = ea3;
    	copy.val1 = val1;
    	copy.val2 = val2;
    	copy.val3 = val2;
    	copy.wp = wp;
    }
}