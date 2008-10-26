/**
 * 
 */
package v9t9.tools.llinst;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class DataWordListOperand implements Operand {

	private int[] args;

	public DataWordListOperand(int[] args) {
		this.args = args;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("args = { ");
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append('>');
			builder.append(Utils.toHex4(args[i]));
		}
		builder.append(" }");
		return builder.toString();
	}
	
	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(this, "Unresolvable operand");
	}
}
