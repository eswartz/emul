/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.engine.cpu.IRawInstructionFactory;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class RawInstructionFactory9900 implements IRawInstructionFactory {

	final static public RawInstructionFactory9900 INSTANCE = new RawInstructionFactory9900();
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#decodeInstruction(int, v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public RawInstruction decodeInstruction(int pc, MemoryDomain domain) {
		return InstTable9900.decodeInstruction(domain.flatReadWord(pc), pc, domain);
	}
	
}
