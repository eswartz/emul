/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.common.Block;
import v9t9.tools.asm.common.HighLevelInstruction;

/**
 * @author ejs
 *
 */
public class LLBlock {


	static int nextId;
	
	private int id;

	private ISymbol label;
	private List<LLInstr> instrs = new LinkedList<LLInstr>(); 
    
    public List<LLBlock> succ;
    public List<LLBlock> pred;
    
    public static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    private int flags;

    /**
     * Create a block
     * @param symbol 
     * @param inst
     */
    public LLBlock(ISymbol symbol) {
    	setLabel(symbol);
    	this.id = nextId++;
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
}
