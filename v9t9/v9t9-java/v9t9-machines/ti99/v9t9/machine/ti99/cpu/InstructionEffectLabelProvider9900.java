/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.cpu.IInstructionEffectLabelProvider;
import v9t9.common.cpu.InstructionWorkBlock;

/**
 * @author ejs
 *
 */
public class InstructionEffectLabelProvider9900 implements
		IInstructionEffectLabelProvider {

	static final Column[] columns = {
		new AddrColumn(5),
		new InstructionColumn(24),
		new SymbolColumn(6),
		new Column("Op1", Role.OPERAND, 26) {
			@Override
			public String getText(InstructionWorkBlock before,
					InstructionWorkBlock after) {
				BaseMachineOperand mop1 = (BaseMachineOperand) before.inst.getOp1();
				if (mop1 == null || mop1.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return before.formatOpChange(1, after);
			}
		},
		new Column("Op2", Role.OPERAND, 26) {
			@Override
			public String getText(InstructionWorkBlock before,
					InstructionWorkBlock after) {
				BaseMachineOperand mop = (BaseMachineOperand) before.inst.getOp2();
				if (mop == null || mop.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return before.formatOpChange(2, after);
			}
		},
		new Column("Op3", Role.OPERAND, 24) {
			@Override
			public String getText(InstructionWorkBlock before,
					InstructionWorkBlock after) {
				BaseMachineOperand mop = (BaseMachineOperand) before.inst.getOp3();
				if (mop == null || mop.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return before.formatOpChange(3, after);
			}
		},
	};

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionEffectLabelProvider#getColumns()
	 */
	@Override
	public Column[] getColumns() {
		return columns;
	}
}
