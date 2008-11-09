/**
 * 
 */
package v9t9.tools.asm.directive;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author Ed
 *
 */
public class EvenDirective extends Directive {

	public EvenDirective(List<Operand> ops) {
	}
	
	@Override
	public String toString() {
		return "EVEN";
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		// this does not affect nearby labels
		
		assembler.setPc((assembler.getPc() + 1) & 0xfffe);
		setPc(assembler.getPc());

		return new IInstruction[] { this };
	}

}
