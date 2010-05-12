/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLGlobalVariable implements ILLVariable {

	protected final ISymbol symbol;
	private LLType addrType;

	public LLGlobalVariable(ISymbol symbol, TypeEngine typeEngine) {
		this.symbol = symbol;
		addrType = typeEngine.getPointerType(symbol.getType());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol.getName().toString();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#getSymbolType()
	 */
	@Override
	public LLType getValueType() {
		return symbol.getType();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#allocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void allocate(ILLCodeTarget target, LLOperand value) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#deallocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void deallocate(ILLCodeTarget target) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#load(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand load(ILLCodeTarget target) {
		LLOperand symbolOp = new LLSymbolOp(symbol);
		LLTempOp temp = target.newTemp(symbol.getType());
		target.emit(new LLLoadInstr(temp, symbol.getType(), symbolOp));
		return temp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#store(org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void store(ILLCodeTarget target, LLOperand value) {
		LLOperand storageOp = new LLSymbolOp(symbol);
		target.emit(new LLStoreInstr(symbol.getType(), value, storageOp));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#address(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand address(ILLCodeTarget target) {
		return new LLSymbolOp(symbol);
	}


}
