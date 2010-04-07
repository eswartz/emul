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
import org.ejs.eulang.llvm.ops.LLVariableOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLVarArgument implements ILLVariable {

	private LLType addrType;
	private ISymbol addrSymbol;
	private final ISymbol symbol;

	/**
	 * @param symbol
	 * @param typeEngine
	 */
	public LLVarArgument(ISymbol symbol, TypeEngine typeEngine) {
		this.symbol = symbol;
		
		// the pointer to the value through the symbol
		LLType varStorage = typeEngine.getPointerType(symbol.getType());
		addrType = typeEngine.getPointerType(varStorage);
		addrSymbol = symbol.getScope().addTemporary(symbol.getName() + "$va", false);
		addrSymbol.setType(addrType);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol + " @@ " +addrSymbol;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((addrSymbol == null) ? 0 : addrSymbol.hashCode());
		result = prime * result
				+ ((addrType == null) ? 0 : addrType.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLVarArgument other = (LLVarArgument) obj;
		if (addrSymbol == null) {
			if (other.addrSymbol != null)
				return false;
		} else if (!addrSymbol.equals(other.addrSymbol))
			return false;
		if (addrType == null) {
			if (other.addrType != null)
				return false;
		} else if (!addrType.equals(other.addrType))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#allocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void allocate(ILLCodeTarget target, LLOperand address) {
		LLSymbolOp tempOp = new LLSymbolOp(addrSymbol);
		target.emit(new LLAllocaInstr(tempOp, addrSymbol.getType().getSubType()));
		if (address != null)
			target.emit(new LLStoreInstr(addrSymbol.getType().getSubType(), address, tempOp));
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
		// first, load the addr
		LLOperand addr = target.newTemp(addrType);
		target.emit(new LLLoadInstr(addr, addrSymbol.getType().getSubType(), new LLSymbolOp(addrSymbol)));
		
		// now, read through it
		LLOperand valTemp = target.newTemp(symbol.getType());
		target.emit(new LLLoadInstr(valTemp, symbol.getType(), addr));
		return valTemp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#store(org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void store(ILLCodeTarget target, LLOperand value) {
		// first, load the addr
		LLOperand addr = target.newTemp(addrType);
		target.emit(new LLLoadInstr(addr, addrSymbol.getType().getSubType(), new LLSymbolOp(addrSymbol)));
		
		// now, write through it
		target.emit(new LLStoreInstr(symbol.getType(), value, addr));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#address(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand address(ILLCodeTarget target) {
		return null;
	}

}
