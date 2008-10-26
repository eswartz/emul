/**
 * 
 */
package v9t9.tools.asm;

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
	private String string;

	public ResolveException(Operand op) {
		this.op = op;
		this.string = "Unresolved operand";
	}
	public ResolveException(Operand op, String string) {
		this.op = op;
		this.string = string;
	}

	@Override
	public String toString() {
		return string + ": " + op;
	}
	
	@Override
	public String getMessage() {
		return toString();
	}
}
