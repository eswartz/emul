/**
 * 
 */
package v9t9.common.cpu;

import v9t9.common.asm.IMachineOperand;

public class MachineOperandState {
	public MachineOperandState(IMachineOperand mop) {
		this.mop = mop;
	}
	/** operand */
	public final IMachineOperand mop;
	/** cycles used by the operand fetch */
	public int cycles;
	/** IChangeElement sets this */
	public short ea;
	/** IChangeElement sets this */
	public short value;
	/** Original value for revert */
	public short prev;
}