/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class StringOperand implements AssemblerOperand {

	private String string;

	public StringOperand(String string) {
		this.string = string;
	}
	
	@Override
	public String toString() {
		return '"' + string + '"';
	}
	
	public String getString() {
		return string;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.Operand#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.RawInstruction)
	 */
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(this, "Cannot resolve a string outside DB, BYTE, TEXT, etc.");
	}

}
