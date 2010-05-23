/**
 * 
 */
package v9t9.tools.asm.assembler;



/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	public HLInstruction() {
	}

	@Override
	public byte[] getBytes() throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}
}
