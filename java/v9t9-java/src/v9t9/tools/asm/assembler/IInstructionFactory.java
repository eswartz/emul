/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public interface IInstructionFactory {
	RawInstruction createRawInstruction(LLInstruction inst) throws ResolveException;

	byte[] encodeInstruction(RawInstruction instruction);

	boolean supportsOp(int inst, int num, AssemblerOperand op);

	boolean isByteInst(int inst);

	boolean isJumpInst(int inst);

	String getInstName(int inst);

	/** Get the expected size of the instruction.  If operands are unresolved,
	 * aim high.
	 * @param target
	 * @return
	 */
	int getInstSize(LLInstruction ins);
	
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
}
