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

	int getPc();

	String toInfoString();

	boolean isByteOp();
	
	/** Get the bytes of a resolved instruction */
	//byte[] getBytes();
}
