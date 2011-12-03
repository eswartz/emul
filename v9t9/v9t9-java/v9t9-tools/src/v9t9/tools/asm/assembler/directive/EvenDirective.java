/**
 * 
 */
package v9t9.tools.asm.assembler.directive;

import java.util.List;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;

/**
 * @author Ed
 *
 */
public class EvenDirective extends Directive {

	/**
	 * @param ops  
	 */
	public EvenDirective(List<IOperand> ops) {
	}
	
	@Override
	public String toString() {
		return "EVEN";
	}

	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		// this does not affect nearby labels
		
		assembler.setPc((assembler.getPc() + 1) & 0xfffe);
		setPc(assembler.getPc());

		return new IInstruction[] { this };
	}

}
