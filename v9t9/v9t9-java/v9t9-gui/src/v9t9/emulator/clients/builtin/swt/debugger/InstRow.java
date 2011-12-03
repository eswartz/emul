/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.InstructionWorkBlock9900;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class InstRow {

	private static int gCounter;
	private final int count = gCounter++;
	private final BaseInstructionWorkBlock before;
	private final BaseInstructionWorkBlock after;
	/**
	 * @param before
	 * @param after
	 */
	public InstRow(BaseInstructionWorkBlock before, BaseInstructionWorkBlock after) {
		this.before = before;
		this.after = after;
	}

	/**
	 * @return
	 */
	public String getAddress() {
		String addr = ">" + HexUtils.toHex4(before.pc);
		
		MemoryEntry entry = before.domain.getEntryAt(before.pc);
		if (entry != null) { 
			String name = entry.lookupSymbol((short) (before.pc & 0xfffe));
			if (name != null) {
				return name + " " + addr;
			}
		}
		return addr;
	}

	/**
	 * @return
	 */
	public String getInst() {
		return before.inst.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InstRow other = (InstRow) obj;
		if (count != other.count) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public String getOp1() {
		BaseMachineOperand mop1 = (BaseMachineOperand) before.inst.getOp1();
		if (mop1 == null || mop1.type == MachineOperand.OP_NONE 
				|| !(before instanceof InstructionWorkBlock9900)
				|| !(after instanceof InstructionWorkBlock9900)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		if (mop1.dest != Operand.OP_DEST_KILLED) {
			builder.append(mop1.valueString(((InstructionWorkBlock9900)before).ea1, ((InstructionWorkBlock9900) before).val1));
		}
		if (mop1.dest != Operand.OP_DEST_FALSE) {
			if (builder.length() > 0)
				builder.append(" => ");
			builder.append(mop1.valueString(((InstructionWorkBlock9900)after).ea1, ((InstructionWorkBlock9900) after).val1));
		}
		return builder.toString();
	}

	public String getOp2() {
		BaseMachineOperand mop2 = (BaseMachineOperand) before.inst.getOp2();
		if (mop2 == null || mop2.type == MachineOperand.OP_NONE 
				|| !(before instanceof InstructionWorkBlock9900)
				|| !(after instanceof InstructionWorkBlock9900)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		if (mop2.dest != Operand.OP_DEST_KILLED) {
			builder.append(mop2.valueString(((InstructionWorkBlock9900)before).ea2, ((InstructionWorkBlock9900) before).val2));
		}
		if (mop2.dest != Operand.OP_DEST_FALSE) {
			if (builder.length() > 0)
				builder.append(" => ");
			builder.append(mop2.valueString(((InstructionWorkBlock9900)after).ea2, ((InstructionWorkBlock9900) after).val2));
		}
		return builder.toString();
	}

	public String getOp3() {
		BaseMachineOperand mop3 = (BaseMachineOperand) before.inst.getOp3();
		if (mop3 == null || mop3.type == MachineOperand.OP_NONE 
				|| !(before instanceof InstructionWorkBlock9900)
				|| !(after instanceof InstructionWorkBlock9900)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		if (mop3.dest != Operand.OP_DEST_KILLED) {
			builder.append(mop3.valueString(((InstructionWorkBlock9900)before).ea3, ((InstructionWorkBlock9900) before).val3));
		}
		if (mop3.dest != Operand.OP_DEST_FALSE) {
			if (builder.length() > 0)
				builder.append(" => ");
			builder.append(mop3.valueString(((InstructionWorkBlock9900)after).ea3, ((InstructionWorkBlock9900) after).val3));
		}
		return builder.toString();
	}
}
