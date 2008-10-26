/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author Ed
 *
 */
public interface IInstruction {

	IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException;

	int getPc();
	
	/** Get the bytes of a resolved instruction */
	byte[] getBytes();
}
