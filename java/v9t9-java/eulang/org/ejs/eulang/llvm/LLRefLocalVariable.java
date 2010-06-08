/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * A reference counted variable has storage:
 * <p>
 * [[ ptr ]]
 * <p>
 * Where 'ptr' points to:
 * <p>
 * [[ dataptr | refcnt | ... ]]
 * <p>
 *'dataptr' points to the actual content. 'refcnt' is the current reference
 * count.  'size' is the data size.  (Or perhaps there are object-size tables)  
 * <p>
 * 'dataptr' may conceivably change at any time, but 'ptr' is a static
 * location in an object allocation table.
 * 
 * @author ejs
 * 
 */
public class LLRefLocalVariable implements ILLVariable {
	private ISymbol objRefSymbol;
	private final ISymbol symbol;
	private LLType symbolValueType;
	private LLType symbolPtrType;

	/**
	 * @param symbol
	 * @param typeEngine
	 */
	public LLRefLocalVariable(ISymbol symbol, TypeEngine typeEngine) {
		this.symbol = symbol;
		
		symbolValueType = symbol.getType().getSubType();
		
		this.symbolPtrType = typeEngine.getPointerType(symbolValueType);
		
		// the pointer to the object entry through the symbol
		LLType objRefStorage = typeEngine.getPointerType(symbol.getType());
		objRefSymbol = symbol.getScope().addTemporary(symbol.getName() + "$obj");
		objRefSymbol.setType(objRefStorage);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol + " ref " +objRefSymbol;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objRefSymbol == null) ? 0 : objRefSymbol.hashCode());
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
		LLRefLocalVariable other = (LLRefLocalVariable) obj;
		if (objRefSymbol == null) {
			if (other.objRefSymbol != null)
				return false;
		} else if (!objRefSymbol.equals(other.objRefSymbol))
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
		return symbolValueType;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#allocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void allocate(ILLCodeTarget target, LLOperand address) {
		LLSymbolOp tempOp = new LLSymbolOp(objRefSymbol);
		target.emit(new LLAllocaInstr(tempOp, symbol.getType()));
		if (address == null) {
			LLOperand nullTemp = target.newTemp(symbol.getType());
			address = new LLConstOp(target.getTypeEngine().INT, 0);
			target.emit(new LLCastInstr(nullTemp, ECast.PTRTOINT, target.getTarget().getTypeEngine().INT, address,
					symbol.getType()));
			address = nullTemp;
		}
		target.emit(new LLStoreInstr(symbol.getType(), address, tempOp));
		if (!(address instanceof LLConstOp)) {
			target.getTarget().incRef(target, symbol.getType(), address);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#deallocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void deallocate(ILLCodeTarget target) {
		LLOperand value = target.newTemp(objRefSymbol.getType());
		target.emit(new LLLoadInstr(value, symbol.getType(), new LLSymbolOp(objRefSymbol)));
		target.getTarget().decRef(target, symbol.getType(), value);
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
		
		// now, read through it to get the value
		LLOperand valTemp = target.newTemp(symbolValueType);
		target.emit(new LLLoadInstr(valTemp, symbolValueType, addr));
		return valTemp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#store(org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void store(ILLCodeTarget target, LLOperand value) {
		LLOperand addr = address(target);
		target.emit(new LLStoreInstr(symbolValueType, value, addr));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#address(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand address(ILLCodeTarget target) {
		// first, load the object id
		LLOperand valueTemp = target.newTemp(objRefSymbol.getType());
		target.emit(new LLLoadInstr(valueTemp, symbol.getType(), new LLSymbolOp(objRefSymbol)));
		
		// dereference to get the data ptr
		LLOperand addrTemp = target.newTemp(symbol.getType());
		target.emit(new LLGetElementPtrInstr(addrTemp, symbol.getType(), valueTemp,
				new LLConstOp(0), new LLConstOp(0)));
		
		// now read data ptr
		LLOperand addr = target.newTemp(symbolPtrType);
		target.emit(new LLLoadInstr(addr, symbolPtrType, addrTemp));
		return addr;
	}


}
