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
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLVarArgument2 implements ILLVariable {

	private ISymbol addrSymbol;
	private final LLLocalVariable local;

	/**
	 * @param symbol
	 * @param typeEngine
	 */
	public LLVarArgument2(ISymbol symbol, TypeEngine typeEngine) {
		ISymbol localSymbol = symbol.getScope().addTemporary(symbol.getName());
		localSymbol.setType(typeEngine.getPointerType(symbol.getType()));
		this.local = new LLLocalVariable(localSymbol, typeEngine);
		
		// the pointer to the value through the symbol
		LLType varStorage = typeEngine.getPointerType(local.getSymbol().getType());
		addrSymbol = local.getSymbol().getScope().addTemporary(local.getSymbol().getName() + "$va", false);
		addrSymbol.setType(typeEngine.getPointerType(varStorage));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return local.getSymbol() + " @@ " +addrSymbol;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((addrSymbol == null) ? 0 : addrSymbol.hashCode());
		result = prime * result
				+ ((addrSymbol.getType() == null) ? 0 : addrSymbol.getType().hashCode());
		result = prime * result + ((local == null) ? 0 : local.hashCode());
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
		LLVarArgument2 other = (LLVarArgument2) obj;
		if (addrSymbol == null) {
			if (other.addrSymbol != null)
				return false;
		} else if (!addrSymbol.equals(other.addrSymbol))
			return false;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#getSymbolType()
	 */
	@Override
	public LLType getValueType() {
		return local.getValueType();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#allocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void allocate(ILLCodeTarget target, LLOperand address) {
		local.allocate(target, address);

		if (address != null) {
			LLSymbolOp tempOp = new LLSymbolOp(addrSymbol);
			target.emit(new LLAllocaInstr(tempOp, addrSymbol.getType().getSubType()));
			LLOperand addr = local.address(target);
			//target.emit(new LLStoreInstr(addrSymbol.getType().getSubType(), address, addr));

			target.emit(new LLStoreInstr(addrSymbol.getType().getSubType(), addr, tempOp));
		}
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
		return local.getSymbol();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#load(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand load(ILLCodeTarget target) {
		LLOperand addr = local.address(target);
		
		// now, read through it
		LLOperand valTemp = target.newTemp(local.getSymbol().getType());
		target.emit(new LLLoadInstr(valTemp, local.getSymbol().getType(), addr));
		return valTemp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLVariable#store(org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void store(ILLCodeTarget target, LLOperand value) {
		LLOperand addr = local.address(target);
		
		// now, write through it
		target.emit(new LLStoreInstr(local.getSymbol().getType(), value, addr));
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
