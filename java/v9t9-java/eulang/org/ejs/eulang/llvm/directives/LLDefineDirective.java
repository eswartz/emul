/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.FNEG;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLParamAttrs;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalSymbol;
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

	private IScope localScope;
	private Map<ISymbol, LLBlock> labelMap;
	private Map<ISymbol, ISymbol> localMap;
	
	private LLBlock current;
	private int tempId;
	
	public LLDefineDirective(IScope localScope, ISymbol symbol, LLLinkage linkage, LLVisibility visibility, String cconv,
			LLAttrType retType, LLAttrType argTypes[], LLFuncAttrs funcAttrs, String section, int align, String gc) {
		this.localScope = localScope;
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
		localMap = new LinkedHashMap<ISymbol, ISymbol>();
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
	 * Create a new block whose label is the given symbol
	 */
	public LLBlock addBlock(ISymbol symbol) {
		LLBlock block = new LLBlock(symbol);
		labelMap.put(symbol, block);
		current = block;
		blocks.add(block);
		return block;
	}

	public LLBlock getCurrentBlock() {
		return current;
	}

	public void setCurrentBlock(LLBlock block) {
		current = block;
	}

	/**
	 * Get the next temporary id.  These must be used in order.
	 * @return
	 */
	public ISymbol newTemp() {
		ISymbol symbol = localScope.addTemporary(tempId + "", false);
		tempId++;
		return symbol;
	}




	/**
	 * Map a given local symbol to a temporary memory location for use in holding its changing value.
	 * This is required for any local which is assigned more than once.
	 * @param isVar variable is indirected twice
	 * @param local
	 * @return temp
	 */
	public ISymbol mapLocalToStore(ISymbol symbol, TypeEngine typeEngine, boolean isVar) {
		ISymbol temp = localMap.get(symbol);
		if (temp == null) {
			LLType addrType = typeEngine.getPointerType(symbol.getType());
			if (isVar)
				addrType = typeEngine.getPointerType(addrType);
			temp = localScope.addTemporary(symbol.getName() + (isVar ? "$va" : "$a"), false);
			temp.setType(addrType);
			localMap.put(symbol, temp);
		}
		return temp;
	}




	/**
	 * @param srcSymbol
	 * @return
	 */
	public ISymbol lookupLocalStore(ISymbol srcSymbol) {
		return localMap.get(srcSymbol);
	}
	

	
}
