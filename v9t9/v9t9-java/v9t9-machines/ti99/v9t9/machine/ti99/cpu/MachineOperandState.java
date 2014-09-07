/**
 * 
 */
package v9t9.machine.ti99.cpu;

public class MachineOperandState {
	public MachineOperandState(MachineOperand9900 mop) {
		this.mop = mop;
	}
	/** operand */
	public final MachineOperand9900 mop;
	/** cycles used by the operand fetch */
	public int cycles;
	/** internal calculation: the effective address of the operand */
	public short ea;
	/** IChangeElement sets this */
	public short value;
}