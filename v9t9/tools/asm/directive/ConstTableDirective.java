/**
 * 
 */
package v9t9.tools.asm.directive;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.Symbol;

/**
 * Indicate where the const table should live (automatically added)
 * @author Ed
 *
 */
public class ConstTableDirective extends Directive {

	private final Assembler assembler;

	public ConstTableDirective(Assembler assembler) {
		this.assembler = assembler;
		
	}
	@Override
	public String toString() {
		return ("$CONSTTABLE");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.BaseAssemblerInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction, boolean)
	 */
	@Override
	public IInstruction[] resolve(Assembler assembler, IInstruction previous,
			boolean finalPass) throws ResolveException {
		
		Symbol symbol = assembler.getConstTable().getTableAddr();
		if (symbol == null) {
			return NO_INSTRUCTIONS;
		}
		
		// go even
		assembler.setPc((assembler.getPc() + 1) & 0xfffe);
		setPc(assembler.getPc());

		return new IInstruction[] { this };
	}
	
	public byte[] getBytes() {
		return assembler.getConstTable().getBytes();
	}
}
