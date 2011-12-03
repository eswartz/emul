/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;

public final class InstructionWorkBlock9900 extends InstructionWorkBlock {
    /** EAs for operands 1 and 2 */
    public short ea1, ea2, ea3;
    /** values for operands 1 and 2 (in: EAs or values, out: value)
    for MPY/DIV, val3 holds lo reg */
    public short val1, val2, val3;	
    public short wp;
    
    public InstructionWorkBlock9900(ICpuState cpu) {
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
    
	public InstructionWorkBlock copy() {
		InstructionWorkBlock block = new InstructionWorkBlock9900(cpu);
		this.copyTo(block);
		return block;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.InstructionWorkBlock#formatOpChange(int)
	 */
	@Override
	public String formatOpChange(int i, InstructionWorkBlock after) {
		StringBuilder builder = new StringBuilder();
		IMachineOperand op = (IMachineOperand) (i == 1 ? inst.getOp1() : (i == 2 ? inst.getOp2() : inst.getOp3()));
		int dest = ((MachineOperand9900) op).dest;
		if (dest != IOperand.OP_DEST_KILLED) {
			builder.append(op.valueString(
					ea1, 
					val1));
		}
		if (dest != IOperand.OP_DEST_FALSE) {
			if (builder.length() > 0)
				builder.append(" => ");
			builder.append(op.valueString(
					((InstructionWorkBlock9900)after).ea1, 
					((InstructionWorkBlock9900) after).val1));
		}
		return builder.toString();

	}
}