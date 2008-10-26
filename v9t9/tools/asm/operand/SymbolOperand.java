/**
 * 
 */
package v9t9.tools.asm.operand;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.Symbol;

/**
 * @author ejs
 *
 */
public class SymbolOperand implements AssemblerOperand {

	private final Symbol symbol;

	public SymbolOperand(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SymbolOperand other = (SymbolOperand) obj;
		if (symbol == null) {
			if (other.symbol != null) {
				return false;
			}
		} else if (!symbol.equals(other.symbol)) {
			return false;
		}
		return true;
	}
	
	public MachineOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		assembler.noteSymbolReference(symbol);
		return MachineOperand.createSymbolImmediate(symbol);
	}

	public Symbol getSymbol() {
		return symbol;
	}
	
}
