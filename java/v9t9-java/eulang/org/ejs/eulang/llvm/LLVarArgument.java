/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLVarArgument implements ILLVariable {

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
		addrSymbol = symbol.getScope().addTemporary(symbol.getName() + "$va", false);
		addrSymbol.setType(typeEngine.getPointerType(varStorage));
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
				+ ((addrSymbol.getType() == null) ? 0 : addrSymbol.getType().hashCode());
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
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
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
	public void allocate(ILLCodeTarget target, LLOperand address) {
		LLSymbolOp tempOp = new LLSymbolOp(addrSymbol);
		target.emit(new LLAllocaInstr(tempOp, addrSymbol.getType().getSubType()));
		if (address == null) {
			LLOperand nullTemp = target.newTemp(addrSymbol.getType().getSubType());
			address = new LLConstOp(0);
			target.emit(new LLCastInstr(nullTemp, ECast.PTRTOINT, target.getTarget().getTypeEngine().INT, address,
					addrSymbol.getType().getSubType()));
			address = nullTemp;
		}
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
		LLOperand addr = address(target);
		
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
		LLOperand addr = address(target);
		
		// now, write through it
		target.emit(new LLStoreInstr(symbol.getType(), value, addr));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#address(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand address(ILLCodeTarget target) {
		// first, load the addr
		LLOperand addr = target.newTemp(addrSymbol.getType());
		target.emit(new LLLoadInstr(addr, addrSymbol.getType().getSubType(), new LLSymbolOp(addrSymbol)));
		return addr;
	}

}
