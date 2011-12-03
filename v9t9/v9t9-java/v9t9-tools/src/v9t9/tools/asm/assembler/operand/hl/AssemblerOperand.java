/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;


/**
 * @author ejs
 *
 */
public interface AssemblerOperand extends IOperand {
	/** 
	 * Resolve self to an LLOperand
	 * @param inst
	 * @return new LLOperand or self
	 * @throws ResolveException if cannot resolve
	 */
	LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException;

	/**
	 * Is this classified as a register?
	 */
	boolean isRegister();
	
	/**
	 * Is this classified as memory?
	 */
	boolean isMemory();
	
	boolean isConst();
	
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
