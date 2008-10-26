/**
 * 
 */
package v9t9.tools.asm.directive;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.NumberOperand;
import v9t9.tools.asm.operand.StringOperand;

/**
 * @author Ed
 *
 */
public class DefineByteDirective extends AssemblerDirective {

	private List<Operand> ops;

	public DefineByteDirective(List<Operand> ops) {
		this.ops = new ArrayList<Operand>();
		for (Operand op : ops) {
			if (op instanceof StringOperand) {
				decodeString(this.ops, ((StringOperand) op).getString());
			} else {
				this.ops.add(op);
			}
		}
	}
	
	private void decodeString(List<Operand> ops, String string) {
		int idx = 0;
		while (idx < string.length()) {
			char ch = string.charAt(idx++);
			if (ch == '\\' && idx < string.length()) {
				ch = string.charAt(idx++);
				switch (ch) {
				case 'n': ch = '\n'; break;
				case 't': ch = '\t'; break;
				case 'r': ch = '\r'; break;
				case '\\': ch = '\\'; break;
				default:
					ops.add(new NumberOperand('\\'));
				}
			}
			ops.add(new NumberOperand(ch));
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DB ");
		boolean first = true;
		for (Operand op : ops) {
			if (first)
				first = false;
			else
				builder.append(", ");
			builder.append(op.toString());
		}			
		return builder.toString();
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		setPc(assembler.getPc());

		for (ListIterator<Operand> iterator = ops.listIterator(); iterator.hasNext();) {
			Operand op = iterator.next();
			MachineOperand mop = op.resolve(assembler, this); 
			if (mop.type != MachineOperand.OP_IMMED)
				throw new ResolveException(op, "Expected number");
			iterator.set(mop);
			assembler.setPc(assembler.getPc() + 1);
		}
		return new IInstruction[] { this };
	}
	
	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[ops.size()];
		int idx = 0;
		for (Operand op : ops) {
			MachineOperand mop = (MachineOperand) op;
			bytes[idx++] = (byte) (mop.immed & 0xff);
		}
		return bytes;
	}
}
