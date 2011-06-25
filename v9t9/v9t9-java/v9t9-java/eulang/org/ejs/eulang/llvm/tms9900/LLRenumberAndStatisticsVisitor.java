/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLAssignInstr;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLTempOp;

/**
 * This assigns a unique number to each LLInstr, finds the usage count for
 * each temp, and defines the codegen flags
 * in {@link LLDefineDirective#flags()}.
 * @author ejs
 *
 */
public class LLRenumberAndStatisticsVisitor extends LLCodeVisitor {
	private int number;

	private List<LLInstr> retInstrs = new ArrayList<LLInstr>();
	private LLDefineDirective def;
	
	private Map<Integer, Integer> tempUses = new TreeMap<Integer, Integer>();
	private Map<Integer, LLAssignInstr> tempDefs = new TreeMap<Integer, LLAssignInstr>();
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterCode(LLDefineDirective directive) {
		this.def = directive;
		def.flags().clear();
		tempUses.clear();
		number = 0;
		return super.enterCode(directive);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public void exitCode(LLDefineDirective directive) {
		for (int tempNum : tempDefs.keySet()) {
			tempDefs.get(tempNum).setUses(tempUses.get(tempNum));
		}
		super.exitCode(directive);
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
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitInstr(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public void exitInstr(LLBlock block, LLInstr instr) {
		if (instr instanceof LLAllocaInstr) {
			
		} else if (instr instanceof LLAssignInstr) {
			if (((LLAssignInstr) instr).getResult() != null) {
				int tempNum = ((LLTempOp)((LLAssignInstr) instr).getResult()).getNumber();
				tempDefs.put(tempNum, (LLAssignInstr) instr);
				tempUses.put(tempNum, 0);
			}
		}
		super.exitInstr(block, instr);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterOperand(org.ejs.eulang.llvm.instrs.LLInstr, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public boolean enterOperand(LLInstr instr, int num, LLOperand operand) {
		if (operand instanceof LLTempOp) {
			// should be here if set
			int tempNum = ((LLTempOp) operand).getNumber();
			int count = tempUses.get(tempNum) + 1;
			tempUses.put(tempNum, count);
		}
		return super.enterOperand(instr, num, operand);
	}
	
}
