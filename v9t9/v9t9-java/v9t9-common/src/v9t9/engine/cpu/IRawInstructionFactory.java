/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
}
