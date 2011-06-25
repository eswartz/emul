/**
 * 
 */
package v9t9.tools.asm.assembler;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

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

	public ResolveException(IInstruction inst, LLOperand op1,
			String string) {
		this.op = op1;
		this.string = string + ": >" + HexUtils.toHex4(inst.getPc()) + "=" + inst.toString();
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
