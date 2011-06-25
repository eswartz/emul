/**
 * 
 */
package v9t9.tools.asm.assembler.directive;

import v9t9.engine.cpu.Instruction9900;
import v9t9.tools.asm.assembler.BaseAssemblerInstruction;
import v9t9.tools.asm.assembler.IInstructionFactory;
import v9t9.tools.asm.assembler.ResolveException;

/**
 * @author Ed
 *
 */
public abstract class Directive extends BaseAssemblerInstruction {
	protected static Instruction9900[] NO_INSTRUCTIONS = new Instruction9900[0];
	public Directive() {
	}
	
	@Override
	public byte[] getBytes(IInstructionFactory factory) throws ResolveException {
		return NO_BYTES;
	}
	
	public boolean isByteOp() {
		return false;
	}
}
