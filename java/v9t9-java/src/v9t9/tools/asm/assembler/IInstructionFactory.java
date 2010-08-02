/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.RawInstruction;

/**
 * @author Ed
 *
 */
public interface IInstructionFactory {
	RawInstruction createRawInstruction(LLInstruction inst) throws ResolveException;
}
