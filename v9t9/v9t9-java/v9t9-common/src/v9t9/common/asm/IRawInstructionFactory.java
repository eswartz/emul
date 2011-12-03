/**
 * 
 */
package v9t9.common.asm;

import v9t9.common.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
}
