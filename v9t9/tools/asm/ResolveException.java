/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.Operand;

/**
 * @author ejs
 *
 */
public class ResolveException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3289343375936751890L;
	private final Operand op;
	private final Instruction inst;
	private String string;

	public ResolveException(Instruction inst, Operand op) {
		this.inst = inst;
		this.op = op;
		this.string = "Unresolved operand";
	}

	public ResolveException(Instruction inst, Operand op,
			String string) {
		this(inst, op);
		this.string = string;
	}

	@Override
	public String toString() {
		return string + ": " + op + " in " + inst;
	}
}
