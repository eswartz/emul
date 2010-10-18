/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuF99;

public final class InstructionWorkBlockF99 extends BaseInstructionWorkBlock {
    
    public short sp;
	public short rp;
	public short op;

	public InstructionWorkBlockF99(CpuF99 cpu) {
    	super(cpu);
	}
    
    public void copyTo(InstructionWorkBlockF99 copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.op = op;
    }
}