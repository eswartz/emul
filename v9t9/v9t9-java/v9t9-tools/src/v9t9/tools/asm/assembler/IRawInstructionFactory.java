/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
}
