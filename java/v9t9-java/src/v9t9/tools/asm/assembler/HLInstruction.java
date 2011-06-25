/**
 * 
 */
package v9t9.tools.asm.assembler;



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
	public byte[] getBytes(IInstructionFactory factory) throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}

	
}
