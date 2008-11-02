/**
 * 
 */
package v9t9.tools.asm;


/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	public HLInstruction() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.AssemblerInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction, boolean)
	 */
	

	@Override
	public byte[] getBytes() throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}
}
