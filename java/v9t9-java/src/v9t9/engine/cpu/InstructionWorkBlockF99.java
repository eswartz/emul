/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.CpuStateF99;
import v9t9.emulator.runtime.interpreter.InterpreterF99;

public final class InstructionWorkBlockF99 extends BaseInstructionWorkBlock {
    
    public short sp;
	public short rp;
	public short opword;
	public int index;
	public boolean showSymbol;
	
	public short[] inStack = new short[4];
	public short[] inReturnStack = new short[4];
	public int instNum;

	public InstructionWorkBlockF99(CpuF99 cpu) {
    	super(cpu.getState());
	}
    
    public void copyTo(InstructionWorkBlockF99 copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.opword = opword;
    	copy.index = index;
    	copy.instNum = instNum;
    	copy.showSymbol = showSymbol;
    	System.arraycopy(inStack, 0, copy.inStack, 0, copy.inStack.length);
    	System.arraycopy(inReturnStack, 0, copy.inReturnStack, 0, copy.inReturnStack.length);
    }

	public short getStackEntry(int i) {
		return domain.readWord(((CpuStateF99)cpu).getSP() + i * 2);
	}
	public short getReturnStackEntry(int i) {
		return domain.readWord(((CpuStateF99)cpu).getRP() + i * 2);
	}
	

	public int nextField() {
		assert index < 4;
		int findex = InterpreterF99.fieldIndices[index % 3];
		int mask = InterpreterF99.fieldMasks[index % 3];
		int field;
		if (index < 3)
			field = (opword >> findex) & mask;
		else {
			field = (domain.readWord(pc) >> findex) & mask;
		}
		index++;
		return field;
	}
	/**
	 * @return
	 */
	public int nextSignedField() {
		assert index < 4;
		int mask = InterpreterF99.fieldMasks[index % 3];
		int findex = InterpreterF99.fieldIndices[index % 3];
		int field;
		if (index < 3)
			field = (opword >> findex) & mask;
		else {
			field = (domain.readWord(pc) >> findex) & mask;
		}
		if ((field & ~(mask >> 1)) != 0) {
			field |= ~mask;
		}
		index++;
		
		return field;
	}
	/**
	 * @return
	 */
	public int nextWord() {
		int thePC = pc;
		pc += 2;
		return domain.readWord(thePC);
	}
}