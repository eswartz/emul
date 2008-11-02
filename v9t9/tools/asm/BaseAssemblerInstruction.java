/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.IInstruction;
import v9t9.utils.Utils;

/**
 * @author Ed
 *
 */
public abstract class BaseAssemblerInstruction extends BaseInstruction {

	protected int pc;

	public abstract IInstruction[] resolve(
			Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException;
	
	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public String toInfoString() {
		return ">" + Utils.toHex4(pc) + " " + toString();
	}

	protected static final byte[] NO_BYTES = new byte[0];
	abstract public byte[] getBytes() throws ResolveException;
}
