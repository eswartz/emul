/**
 * 
 */
package v9t9.tools.asm.assembler;


import v9t9.base.utils.HexUtils;
import v9t9.common.asm.BaseInstruction;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;

/**
 * @author Ed
 *
 */
public abstract class BaseAssemblerInstruction extends BaseInstruction {

	protected int pc;

	public abstract IInstruction[] resolve(
			IAssembler assembler, IInstruction previous, boolean finalPass) throws ResolveException;
	
	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public String toInfoString() {
		return ">" + HexUtils.toHex4(pc) + " " + toString();
	}

	protected static final byte[] NO_BYTES = new byte[0];
	abstract public byte[] getBytes(IAsmInstructionFactory instFactory) throws ResolveException;

}