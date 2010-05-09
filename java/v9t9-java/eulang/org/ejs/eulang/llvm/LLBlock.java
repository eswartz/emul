/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ejs.eulang.llvm.ILLCodeVisitor.Terminate;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLBlock {
	private ISymbol label;
	private List<LLInstr> instrs = new LinkedList<LLInstr>(); 
    
    public List<LLBlock> succ;
    public List<LLBlock> pred;
    
    public static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    /**
     * Create a block
     * @param symbol 
     * @param inst
     */
    public LLBlock(ISymbol symbol) {
    	setLabel(symbol);
        succ = new ArrayList<LLBlock>(2);
        pred = new ArrayList<LLBlock>(2);
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!instrs.isEmpty()) {
			if (label != null)
				sb.append(label.getLLVMName().substring(1) + ":\n");
			for (LLInstr instr : instrs) {
				sb.append(instr);
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/**
	 * @param symbol the label to set
	 */
	public void setLabel(ISymbol symbol) {
		this.label = symbol;
	}
	/**
	 * @return the label
	 */
	public ISymbol getLabel() {
		return label;
	}
	/**
	 * @return the instrs
	 */
	public List<LLInstr> instrs() {
		return instrs;
	}

	/**
	 * @return
	 */
	public boolean endsWithUncondBranch() {
		return !instrs.isEmpty() && instrs.get(instrs.size() - 1) instanceof LLUncondBranchInstr;
	}

	/**
	 * @param visitor
	 */
	public void accept(ILLCodeVisitor visitor) {
		try {
			if (visitor.enterBlock(this)) {

				for (LLInstr instr : instrs) {
					instr.accept(visitor);
				}
			}
			visitor.exitBlock(this);
		} catch (Terminate e) {
			
		}
	}
}
