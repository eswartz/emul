/**
 * 
 */
package v9t9.machine.f99b.asm;

import java.util.Arrays;

import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;

public final class InstructionWorkBlockF99b extends InstructionWorkBlock {
    
	private static short[] NO_ENTRIES = new short[0];
	
    public short sp;
	public short rp;
	public short up;
	public short lp;
	public boolean showSymbol;
	
	public short[] inStack = NO_ENTRIES;
	public short[] inReturnStack = NO_ENTRIES;

	public InstructionWorkBlockF99b(ICpuState cpuState) {
    	super(cpuState);
	}
    
    public void copyTo(InstructionWorkBlockF99b copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.up = up;
    	copy.lp = lp;
    	copy.showSymbol = showSymbol;
    	copy.inStack = Arrays.copyOf(inStack, inStack.length);
    	copy.inReturnStack = Arrays.copyOf(inReturnStack, inReturnStack.length);
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.cpu.InstructionWorkBlock#copy()
     */
    @Override
    public InstructionWorkBlock copy() {
    	InstructionWorkBlock block = new InstructionWorkBlockF99b(cpu);
		this.copyTo(block);
		return block;
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