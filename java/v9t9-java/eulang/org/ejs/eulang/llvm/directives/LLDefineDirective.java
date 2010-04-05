/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.FNEG;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLParamAttrs;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLDefineDirective extends LLBaseDirective {

	private final ISymbol symbol;
	private final LLLinkage linkage;
	private final LLVisibility visibility;
	private final String cconv;
	private final LLAttrType retType;
	private final LLAttrType[] argTypes;
	private final LLFuncAttrs funcAttrs;
	private final String section;
	private final int align;
	private final String gc;
	private List<LLBlock> blocks;

	private Map<ISymbol, LLBlock> labelMap;
	private LLBlock current;
	private int tempId;
	
	public LLDefineDirective(ISymbol symbol, LLLinkage linkage, LLVisibility visibility, String cconv,
			LLAttrType retType, LLAttrType argTypes[], LLFuncAttrs funcAttrs, String section, int align, String gc) {
		this.symbol = symbol;
		this.linkage = linkage;
		this.visibility = visibility;
		this.cconv = cconv;
		this.retType = retType;
		this.argTypes = argTypes;
		this.funcAttrs = funcAttrs;
		this.section = section;
		this.align = align;
		this.gc = gc;
		
		this.blocks = new ArrayList<LLBlock>();
		labelMap = new LinkedHashMap<ISymbol, LLBlock>();
	}

			
			
			
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		return "define " 
		+ (linkage != null ? linkage.getLinkageName() + " " : "")
		+ (visibility != null ? visibility.getVisibility() + " " : "")
		+ (cconv != null ? cconv + " " : "")
		+ retType.toString() + " "
		+ symbol.getLLVMName()
		+ "(" + argTypeString() + ") "
		+ (funcAttrs != null ? funcAttrs + " " : "")
		+ (section != null ? "section \"" + section + "\" " : "")
		+ (align != 0 ? "align " + align + " " : "")
		+ (gc != null ? "gc \"" + gc + "\" " : "")
		+ "\n{\n"
		+ blockString()
		+ "}\n";
		
	
	}




	/**
	 * @return
	 */
	private String blockString() {
		StringBuilder sb = new StringBuilder();
		for (LLBlock block : blocks) {
			sb.append(block);
		}
		return sb.toString();
	}




	/**
	 * @return
	 */
	private String argTypeString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (LLAttrType argType : argTypes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(argType);
		}
		return sb.toString();
	}

	/**
	 * @return the blocks
	 */
	public List<LLBlock> blocks() {
		return blocks;
	}




	/**
	 * @param string
	 * @return
	 */
	public LLBlock addBlock(ISymbol symbol) {
		LLBlock block = new LLBlock(symbol);
		labelMap.put(symbol, block);
		current = block;
		blocks.add(block);
		return block;
	}

	/**
	 * @return the current
	 */
	public LLBlock getCurrentBlock() {
		return current;
	}


	/**
	 * @param object
	 */
	public void setCurrentBlock(LLBlock block) {
		current = block;
	}




	/**
	 * @return
	 */
	public int nextId() {
		return tempId++;
	}
}
