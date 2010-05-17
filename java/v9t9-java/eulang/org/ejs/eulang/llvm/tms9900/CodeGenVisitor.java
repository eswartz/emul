/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashMap;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.tms9900.asm.Label;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;

import v9t9.tools.asm.assembler.HLInstruction;

/**
 * @author ejs
 *
 */
public class CodeGenVisitor extends LLCodeVisitor {

	private IScope llScope;
	private LocalScope vrScope;
	private Routine routine;
	private Block block;
	private HashMap<ISymbol, Block> blockMap;
	/**
	 * 
	 */
	public CodeGenVisitor() {
		blockMap = new HashMap<ISymbol, Block>();
	}
	/**
	 * @return
	 */
	public Routine getRoutine() {
		return routine;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLBaseDirective)
	 */
	@Override
	public boolean enterCode(LLDefineDirective def) {
		llScope = def.getScope();
		vrScope = new LocalScope(llScope);
		
		routine = new LinkedRoutine(def);
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public boolean enterBlock(LLBlock llblock) {
		ISymbol lllabelSym = llblock.getLabel();
		Label label = new Label(lllabelSym.getUniqueName());
		block = new Block(label);
		blockMap.put(lllabelSym, block);
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public void exitBlock(LLBlock llblock) {
		routine.addBlock(block);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(LLBlock block, LLInstr llinstr) {
		System.out.println(llinstr);

		if (llinstr instanceof LLRetInstr) {
			generateReturn((LLRetInstr) llinstr);
		} else {
			unhandled(llinstr);
		}
		
		return true;
	}
	

	/**
	 * @param llinstr
	 */
	private void unhandled(LLInstr llinstr) {
		throw new IllegalStateException("unhandled instruction: " + llinstr);
	}

	/**
	 * @param llinstr
	 */
	private void generateReturn(LLRetInstr instr) {
		LLOperand[] llops = instr.getOperands();
		if (llops.length == 0) {
			
		} else if (llops.length == 1) {
			
		} else {
			unhandled(instr);
		}
		
		HLInstruction[] rets = routine.generateReturn();
		for (HLInstruction ret : rets)
			block.addInst(ret);
	}

}
