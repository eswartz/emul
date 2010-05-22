/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;

/**
 * This assigns a unique number to each LLInstr and defines the codegen flags
 * in {@link LLDefineDirective#flags()}.
 * @author ejs
 *
 */
public class RenumberAndStatisticsVisitor extends LLCodeVisitor {
	private int number;

	private List<LLInstr> retInstrs = new ArrayList<LLInstr>();
	private LLDefineDirective def;
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterCode(LLDefineDirective directive) {
		this.def = directive;
		def.flags().clear();
		return super.enterCode(directive);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(LLBlock block, LLInstr instr) {
		instr.setNumber(number++);
		if (instr instanceof LLRetInstr) {
			if (!retInstrs.isEmpty()) {
				def.flags().add(LLDefineDirective.MULTI_RET);
			}
			retInstrs.add(instr);
		}
		return false;
	}
	
}
