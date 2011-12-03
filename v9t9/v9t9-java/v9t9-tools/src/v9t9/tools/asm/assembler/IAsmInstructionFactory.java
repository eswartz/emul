/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.asm.IInstructionFactory;
import v9t9.engine.asm.ResolveException;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public interface IAsmInstructionFactory extends IInstructionFactory {

	RawInstruction createRawInstruction(LLInstruction inst) throws ResolveException;
	boolean supportsOp(int inst, int num, AssemblerOperand op);
	/** Get the expected size of the instruction.  If operands are unresolved,
	 * aim high.
	 * @param target
	 * @return
	 */
	int getInstSize(LLInstruction ins);
}
