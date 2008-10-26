/**
 * 
 */
package v9t9.tools.asm.directive;

import java.util.List;
import java.util.ListIterator;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author Ed
 *
 */
public class DefineWordDirective extends AssemblerDirective {

	private List<Operand> ops;

	public DefineWordDirective(List<Operand> ops) {
		this.ops = ops;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DW ");
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
		assembler.setPc((assembler.getPc() + 1) & 0xfffe);
		setPc(assembler.getPc());

		for (ListIterator<Operand> iterator = ops.listIterator(); iterator.hasNext();) {
			Operand op = iterator.next();
			MachineOperand mop = op.resolve(assembler, this); 
			if (mop.type != MachineOperand.OP_IMMED)
				throw new ResolveException(op, "Expected number");
			
			iterator.set(mop);
			assembler.setPc(assembler.getPc() + 2);
		}
		return new IInstruction[] { this };
	}
	
	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[ops.size() * 2];
		int idx = 0;
		for (Operand op : ops) {
			MachineOperand mop = (MachineOperand) op;
			bytes[idx++] = (byte) (mop.immed >> 8);
			bytes[idx++] = (byte) (mop.immed & 0xff);
		}
		return bytes;
	}
}
