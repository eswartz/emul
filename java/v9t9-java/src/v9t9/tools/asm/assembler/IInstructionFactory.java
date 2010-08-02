/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public interface IInstructionFactory {
	RawInstruction createRawInstruction(LLInstruction inst) throws ResolveException;

	byte[] encodeInstruction(RawInstruction instruction);

	boolean supportsOp(int inst, int num, AssemblerOperand op);
}
