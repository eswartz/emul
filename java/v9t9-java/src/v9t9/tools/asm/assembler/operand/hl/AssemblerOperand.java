/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;


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

	/**
	 * Is this classified as a register?
	 */
	boolean isRegister();
	
	/**
	 * Is this classified as memory?
	 */
	boolean isMemory();
	
	/**
	 * Replace the src with dst and return this or a new operand.
	 * @param src
	 * @param dst
	 * @return
	 */
	AssemblerOperand replaceOperand(AssemblerOperand src, AssemblerOperand dst);
	
	AssemblerOperand[] getChildren();

	/**
	 * @param i
	 * @return
	 */
	AssemblerOperand addOffset(int i);
	
	void accept(IOperandVisitor visitor);
}
