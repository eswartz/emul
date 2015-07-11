/*
  InstructionEffectLabelProvider9900.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionEffectLabelProvider;

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
			public String getText(ICpu cpu, ChangeBlock block, boolean beforeExecute) {
				BaseMachineOperand mop1 = (BaseMachineOperand) block.inst.getOp1();
				if (mop1 == null || mop1.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return formatOpChange(1, cpu, block, beforeExecute);
			}
		},
		new Column("Op2", Role.OPERAND, 26) {
			@Override
			public String getText(ICpu cpu, ChangeBlock block, boolean beforeExecute) {
				BaseMachineOperand mop = (BaseMachineOperand) block.inst.getOp2();
				if (mop == null || mop.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return formatOpChange(2, cpu, block, beforeExecute);
			}
		},
		new Column("Op3", Role.OPERAND, 24) {
			@Override
			public String getText(ICpu cpu, ChangeBlock block, boolean beforeExecute) {
				BaseMachineOperand mop = (BaseMachineOperand) block.inst.getOp3();
				if (mop == null || mop.type == IMachineOperand.OP_NONE) {
					return "";
				}
				return formatOpChange(3, cpu, block, beforeExecute);
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

	protected static String formatOpChange(int op, ICpu cpu, ChangeBlock block, boolean beforeExecute) {
		if (beforeExecute) {
			return block.inst.getOp(op).toString();
		}
		else {
			return "???";
		}
	}
}
