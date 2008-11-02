/**
 * 
 */
package v9t9.tools.asm.directive;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.Symbol;

/**
 * @author Ed
 *
 */
public class LabelDirective extends Directive {

	private final Symbol symbol;

	public LabelDirective(Symbol symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String toString() {
		return symbol.getName() + ":";
	}
	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		symbol.setAddr(assembler.getPc());
		setPc(assembler.getPc());

		return new IInstruction[] { this };
	}

	@Override
	public void setPc(int pc) {
		super.setPc(pc);
		symbol.setAddr(pc);
	}
	public Symbol getSymbol() {
		return symbol;
	}
	
}
