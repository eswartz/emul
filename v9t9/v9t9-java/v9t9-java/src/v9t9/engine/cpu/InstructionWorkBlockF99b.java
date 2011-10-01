/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuF99b;
import v9t9.emulator.runtime.cpu.CpuStateF99b;

public final class InstructionWorkBlockF99b extends BaseInstructionWorkBlock {
    
    public short sp;
	public short rp;
	public short up;
	public short lp;
	public boolean showSymbol;
	
	public short[] inStack = new short[6];
	public short[] inReturnStack = new short[6];

	public InstructionWorkBlockF99b(CpuF99b cpu) {
    	super(cpu.getState());
	}
    
    public void copyTo(InstructionWorkBlockF99b copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.up = up;
    	copy.lp = lp;
    	copy.showSymbol = showSymbol;
    	//copy.inStack = Arrays.copyOf(inStack, inStack.length);
    	//copy.inReturnStack = Arrays.copyOf(inReturnStack, inReturnStack.length);
    	System.arraycopy(inStack, 0, copy.inStack, 0, copy.inStack.length);
    	System.arraycopy(inReturnStack, 0, copy.inReturnStack, 0, copy.inReturnStack.length);
    }

	public short getStackEntry(int i) {
		return domain.readWord(((CpuStateF99b)cpu).getSP() + i * 2);
	}
	public short getReturnStackEntry(int i) {
		return domain.readWord(((CpuStateF99b)cpu).getRP() + i * 2);
	}
	public short getLocalStackEntry(int i) {
		return domain.readWord(((CpuStateF99b)cpu).getLP() - (i + 1) * 2);
	}

	/**
	 * @return
	 */
	public int nextByte() {
		return domain.readByte(pc++) & 0xff;
	}
	/**
	 * @return
	 */
	public int nextWord() {
		return (domain.readByte(pc++) << 8) | domain.readByte(pc++) & 0xff;
	}
	
}