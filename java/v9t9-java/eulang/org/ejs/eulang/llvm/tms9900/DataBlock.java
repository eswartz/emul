/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.llvm.tms9900.asm.AsmOperand;
import org.ejs.eulang.symbols.ISymbol;

/**
 * 
 * @author ejs
 *
 */
public class DataBlock {

	private final ISymbol name;
	private AsmOperand value;
	
	public DataBlock(ISymbol name, AsmOperand value) {
		this.name = name;
		this.value = value;
	}
	
	public AsmOperand getValue() {
		return value;
	}
	public void setValue(AsmOperand value) {
		this.value = value;
	}
	public ISymbol getName() {
		return name;
	}

}
