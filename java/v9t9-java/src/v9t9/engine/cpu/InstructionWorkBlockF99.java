/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuF99;

public final class InstructionWorkBlockF99 extends BaseInstructionWorkBlock {
    
    public short sp;
	public short rp;
	public short op;
	
	public short[] inStack = new short[4];
	public short[] inReturnStack = new short[4];

	public InstructionWorkBlockF99(CpuF99 cpu) {
    	super(cpu);
	}
    
    public void copyTo(InstructionWorkBlockF99 copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.op = op;
    	System.arraycopy(inStack, 0, copy.inStack, 0, copy.inStack.length);
    	System.arraycopy(inReturnStack, 0, copy.inReturnStack, 0, copy.inReturnStack.length);
    }

	public short getStackEntry(int i) {
		return domain.readWord(((CpuF99)cpu).getSP() + i * 2);
	}
	public short getReturnStackEntry(int i) {
		return domain.readWord(((CpuF99)cpu).getRSP() + i * 2);
	}
}