/**
 * 
 */
package v9t9.tools.asm.assembler.directive;

import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.BaseAssemblerInstruction;
import v9t9.tools.asm.assembler.IInstructionFactory;
import v9t9.tools.asm.assembler.ResolveException;

/**
 * @author Ed
 *
 */
public abstract class Directive extends BaseAssemblerInstruction {
	protected static RawInstruction[] NO_INSTRUCTIONS = new RawInstruction[0];
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
