/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.ILLVariable;
import org.ejs.eulang.llvm.LLArgAttrType;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.llvm.instrs.LLBaseInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.ops.LLBitcastOp;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.llvm.ops.LLVariableOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLDefineDirective extends LLBaseDirective implements ILLCodeTarget {

	/** Multiple return instructions exist */
	public static final String MULTI_RET = "multiRet";
	
	private final ISymbol symbol;
	private final LLLinkage linkage;
	private final LLVisibility visibility;
	private final String cconv;
	private final LLAttrType retType;
	private final LLArgAttrType[] argTypes;
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
	private final LLVMGenerator generator;
	private FunctionConvention convention;

	private Set<String> flags = new HashSet<String>();
	
	public LLDefineDirective(LLVMGenerator generator, ITarget target, LLModule module, IScope localScope,
			ISymbol symbol, LLLinkage linkage, LLVisibility visibility, String cconv, LLAttrType retType,
			LLArgAttrType argTypes[], LLFuncAttrs funcAttrs, String section, int align, String gc) {
		this.generator = generator;
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
		this.convention = new FunctionConvention(linkage, visibility, cconv, retType, argTypes, funcAttrs, gc);
		
		this.blocks = new ArrayList<LLBlock>();
		labelMap = new LinkedHashMap<ISymbol, LLBlock>();
	}

			
			
	public FunctionConvention getConvention() {
		return convention;
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
		ISymbol symbol = localScope.add(tempId + "", false);
		tempId++;
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#newTemp(org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLTempOp newTemp(LLType type) {
		return new LLTempOp(tempId++, type);
	}

	

	public void emit( LLBaseInstr instr) {
		for (LLOperand op : instr.getOperands()) {
			if (op != null)
				module.emitTypes(op.getType());
		}
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
			if (var.getValueType().equals(valueType)) {
				var.store(this, value);
				return;
			}
			target = var.load(this);
		} 
		if (valueType != null && valueType.equals(target.getType().getSubType())) {
			// TODO: this is copied from ILLVariable impls
			
			if (target.getType().getBasicType() == BasicType.REF) {
				// dereference to get the data ptr
				LLOperand addrTemp = newTemp(target.getType());
				emit(new LLGetElementPtrInstr(addrTemp, target.getType(), target,
						new LLConstOp(0), new LLConstOp(0)));
				
				// now read data ptr
				LLType valPtrType = getTypeEngine().getPointerType(valueType);
				LLOperand addr = newTemp(valPtrType);
				emit(new LLLoadInstr(addr, valPtrType, addrTemp));
				
				// now store value
				emit(new LLStoreInstr(valueType, value, addr));
			} else if (target.getType().getBasicType() == BasicType.POINTER) {
				emit(new LLStoreInstr(valueType, value, target));
			} else {
				throw new IllegalStateException();
			}
		} else {
			// should already be addressable
			emit(new LLStoreInstr(valueType, value, target));
		}
	}

	/**
	 * Load the value of the given operand, if it is a symbol
	 * @throws ASTException 
	 */
	public LLOperand load(LLType valueType, LLOperand source) throws ASTException {
		if (source instanceof LLVariableOp) { 
			
			ILLVariable var = ((LLVariableOp) source).getVariable();
			if (var.getValueType().equals(valueType))
				return var.load(this);
			
			source = var.load(this);
		} 
		if (valueType != null && valueType.equals(source.getType().getSubType())) {
			// TODO: this is copied from ILLVariable impls
			
			if (source.getType().getBasicType() == BasicType.REF) {
				// dereference to get the data ptr
				LLOperand addrTemp = newTemp(source.getType());
				emit(new LLGetElementPtrInstr(addrTemp, source.getType(), source,
						new LLConstOp(0), new LLConstOp(0)));
				
				// now read data ptr
				LLType valPtrType = getTypeEngine().getPointerType(valueType);
				LLOperand addr = newTemp(valPtrType);
				emit(new LLLoadInstr(addr, valPtrType, addrTemp));
				
				// now read value
				LLOperand value = newTemp(valueType);
				emit(new LLLoadInstr(value, valueType, addr));
				return addr;	
			} else if (source.getType().getBasicType() == BasicType.POINTER) {
				LLOperand ret = newTemp(valueType);
				emit(new LLLoadInstr(ret, valueType, source));
				return ret;
			} else {
				throw new IllegalStateException();
			}
		} else if (valueType != null && !valueType.equals(source.getType())) {
			if (source.getType().getBasicType() == BasicType.VOID || valueType.getBasicType() == BasicType.VOID) {
				// ignore
				return source;
			} else if ((valueType instanceof LLAggregateType || valueType instanceof LLArrayType)
					&& (source.getType() instanceof LLAggregateType || source.getType() instanceof LLArrayType)) {
				
				// HACK!
				if (valueType.matchesExactly(source.getType())) 
					return source;
				
				LLOperand cast = new LLBitcastOp(valueType, source);
				return cast;
			}
			// ignore: ref/ptr casts?
			return source;
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getPreviousBlock()
	 */
	@Override
	public LLBlock getPreviousBlock() {
		return blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getGenerator()
	 */
	@Override
	public LLVMGenerator getGenerator() {
		return generator;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getTypeEngine()
	 */
	@Override
	public TypeEngine getTypeEngine() {
		return generator.getTypeEngine();
	}




	/**
	 * @return
	 */
	public IScope getScope() {
		return localScope;
	}




	/**
	 * @return
	 */
	public ISymbol getName() {
		return symbol;
	}
	
	public void accept(ILLCodeVisitor visitor) {
		if (visitor.enterCode(this)) {

			for (LLBlock block : blocks) {
				block.accept(visitor);
			}
			
		}
		
		visitor.exitCode(this);
	}



	/**
	 * @return
	 */
	public LLBlock getEntryBlock() {
		return blocks.get(0);
	}



	/**
	 * @return
	 */
	public ISymbol getSymbol() {
		return symbol;
	}



	/**
	 * Get flags for the routine as a whole (for code generation)
	 * @return
	 */
	public Set<String> flags() {
		return flags;
	}
}
