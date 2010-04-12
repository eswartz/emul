/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.ILLVariable;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.llvm.instrs.LLBaseInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLVariableOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLDefineDirective extends LLBaseDirective implements ILLCodeTarget {

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
	
	private LLBlock current;
	private int tempId;
	private final ITarget target;
	private final LLModule module;
	
	public LLDefineDirective(ITarget target, LLModule module, IScope localScope,
			ISymbol symbol, LLLinkage linkage, LLVisibility visibility, String cconv, LLAttrType retType,
			LLAttrType argTypes[], LLFuncAttrs funcAttrs, String section, int align, String gc) {
		this.target = target;
		this.module = module;
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.ICodeTarget#blocks()
	 */
	public List<LLBlock> blocks() {
		return blocks;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.ICodeTarget#addBlock(org.ejs.eulang.symbols.ISymbol)
	 */
	public LLBlock addBlock(ISymbol symbol) {
		LLBlock block = new LLBlock(symbol);
		labelMap.put(symbol, block);
		current = block;
		blocks.add(block);
		return block;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.ICodeTarget#getCurrentBlock()
	 */
	public LLBlock getCurrentBlock() {
		return current;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.ICodeTarget#setCurrentBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	public void setCurrentBlock(LLBlock block) {
		current = block;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.ICodeTarget#newTemp()
	 */
	public ISymbol newTempSymbol() {
		//ISymbol symbol = localScope.addTemporary(tempId + "", false);
		ISymbol symbol = localScope.add(tempId + "");
		tempId++;
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#newTemp(org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLSymbolOp newTemp(LLType type) {
		ISymbol newTemp = newTempSymbol();
		newTemp.setType(type);
		return new LLSymbolOp(newTemp);
	}

	

	public void emit( LLBaseInstr instr) {
		getCurrentBlock().instrs().add(instr);
	}
	
	/**
	 * Copy a temporary value into the target symbol.  If the target is a local symbol,
	 * also store
	 * same type or the original variable, which may be on the stack or in memory.
	 */
	public void store(LLType valueType, LLOperand value, LLOperand target) {
		if (target instanceof LLVariableOp) {
			ILLVariable var = ((LLVariableOp) target).getVariable();
			var.store(this, value);
		} else {
			// should already be addressable
			emit(new LLStoreInstr(valueType, value, target));
		}
	}

	/**
	 * Load the value of the given operand, if it is a symbol
	 */
	public LLOperand load(LLType valueType, LLOperand source) {
		if (source instanceof LLVariableOp) { 
			
			ILLVariable var = ((LLVariableOp) source).getVariable();
			return var.load(this);
		} else {
			return source;
		}
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getModule()
	 */
	@Override
	public LLModule getModule() {
		return module;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getTarget()
	 */
	@Override
	public ITarget getTarget() {
		return target;
	}
	
}
