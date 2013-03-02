/*
  InstructionEffectLabelProvider9900.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
