/**
 * 
 */
package v9t9.common.asm;

import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	int getChunkSize();
	RawInstruction decodeInstruction(int pc, IMemoryDomain domain);
}
