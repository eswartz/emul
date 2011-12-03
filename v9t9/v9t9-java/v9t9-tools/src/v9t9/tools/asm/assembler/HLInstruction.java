/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.ResolveException;



/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	/**
	 * @param factory
	 */
	public HLInstruction(IInstructionFactory factory) {
		super(factory);
	}

	@Override
	public byte[] getBytes(IAsmInstructionFactory factory) throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}

	
}
