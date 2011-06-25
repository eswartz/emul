/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.Collections;
import java.util.List;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.llvm.instrs.ECast;
import org.ejs.eulang.llvm.instrs.LLBaseInstr;
import org.ejs.eulang.llvm.ops.LLCastOp;
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
public class StaticDataCodeTarget implements ILLCodeTarget {


	/** Multiple return instructions exist */
	public static final String MULTI_RET = "multiRet";
	
	private final ITarget target;
	private final LLModule module;
	private final LLVMGenerator generator;

	private IScope localScope;

	private final ISymbol symbol;
	
	public StaticDataCodeTarget(LLVMGenerator generator, ISymbol symbol, ITarget target, LLModule module, IScope localScope) {
		this.generator = generator;
		this.symbol = symbol;
		this.target = target;
		this.module = module;
		this.localScope = localScope;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#addBlock(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public LLBlock addBlock(ISymbol symbol) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#blocks()
	 */
	@Override
	public List<LLBlock> blocks() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#emit(org.ejs.eulang.llvm.instrs.LLBaseInstr)
	 */
	@Override
	public void emit(LLBaseInstr instr) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getCurrentBlock()
	 */
	@Override
	public LLBlock getCurrentBlock() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getGenerator()
	 */
	@Override
	public LLVMGenerator getGenerator() {
		return generator;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getModule()
	 */
	@Override
	public LLModule getModule() {
		return module;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getPreviousBlock()
	 */
	@Override
	public LLBlock getPreviousBlock() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getScope()
	 */
	@Override
	public IScope getScope() {
		return localScope;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getTarget()
	 */
	@Override
	public ITarget getTarget() {
		return target;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#getTypeEngine()
	 */
	@Override
	public TypeEngine getTypeEngine() {
		return target.getTypeEngine();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#load(org.ejs.eulang.types.LLType, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public LLOperand load(LLType valueType, LLOperand source)
			throws ASTException {
		if (source instanceof LLVariableOp) { 
			throw new UnsupportedOperationException("cannot reference variables in initializers");
		} 
		if (valueType != null && valueType.equals(source.getType().getSubType())) {
			throw new UnsupportedOperationException("cannot dereference variables in initializers");
		} else if (valueType != null && !valueType.equals(source.getType())) {
			if (source.getType().getBasicType() == BasicType.VOID || valueType.getBasicType() == BasicType.VOID) {
				// ignore
				return source;
			} else if ((valueType instanceof LLAggregateType || valueType instanceof LLArrayType)
					&& (source.getType() instanceof LLAggregateType || source.getType() instanceof LLArrayType)) {
				
				// HACK!
				if (valueType.matchesExactly(source.getType())) 
					return source;
				
				LLOperand cast = new LLCastOp(ECast.BITCAST, valueType, source);
				return cast;
			}
			// ignore: ref/ptr casts?
			return source;
		} else {
			return source;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#newTemp(org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLTempOp newTemp(LLType type) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#setCurrentBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public void setCurrentBlock(LLBlock block) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeTarget#store(org.ejs.eulang.types.LLType, org.ejs.eulang.llvm.ops.LLOperand, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void store(LLType valueType, LLOperand value, LLOperand target) {
		throw new UnsupportedOperationException();
	}

}
