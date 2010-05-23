/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This assigns a unique number to each {@link AsmInstruction} 
 * @author ejs
 *
 */
public class RenumberVisitor extends CodeVisitor {
	private int number;

	public RenumberVisitor() {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.LINEAR;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		number = 0;
		return super.enterRoutine(routine);
	}
	
	@Override
	public boolean enterInstr(Block block, AsmInstruction instr) {
		instr.setNumber(number++);
		return false;
	}
}
