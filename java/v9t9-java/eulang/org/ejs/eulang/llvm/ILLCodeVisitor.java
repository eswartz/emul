/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * Scan LLVM code
 * @author ejs
 *
 */
public interface ILLCodeVisitor {

	class Terminate extends Error {
		private static final long serialVersionUID = 7914440842755205893L;
		
	}
	
	boolean enterCode(LLDefineDirective directive);
	
	void exitCode(LLDefineDirective directive);
	
	boolean enterBlock(LLBlock block);
	
	void exitBlock(LLBlock block);
	
	boolean enterInstr(LLBlock block, LLInstr instr);
	
	void exitInstr(LLBlock block, LLInstr instr);
	
	boolean enterOperand(LLInstr instr, int num, LLOperand operand);
	
	void exitOperand(LLInstr instr, int num, LLOperand operand);
}
