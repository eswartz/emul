/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
}
