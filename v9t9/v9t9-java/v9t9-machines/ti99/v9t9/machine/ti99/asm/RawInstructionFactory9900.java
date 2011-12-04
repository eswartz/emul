/**
 * 
 */
package v9t9.machine.ti99.asm;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.cpu.InstTable9900;

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
	public RawInstruction decodeInstruction(int pc, IMemoryDomain domain) {
		return InstTable9900.decodeInstruction(domain.flatReadWord(pc), pc, domain);
	}
	
}
