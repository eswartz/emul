/**
 * 
 */
package v9t9.tools.asm.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.ll.LLOperand;


/**
 * @author ejs
 *
 */
public interface AssemblerOperand extends Operand {
	/** 
	 * Resolve self to an LLOperand
	 * @param inst
	 * @return new LLOperand or self
	 * @throws ResolveException if cannot resolve
	 */
	LLOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException;
 
}
