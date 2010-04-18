/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLIntType;

/**
 * @author ejs
 *
 */
public class LLLocalArrayVariable extends LLLocalVariable {

	private IAstTypedExpr dynamicArraySize;
	private LLOperand arraySizeOp;
	private LLArrayType arrayType;
	
	public LLLocalArrayVariable(ISymbol symbol, TypeEngine typeEngine) {
		super(symbol, typeEngine);
		this.arrayType = (LLArrayType) symbol.getType();
		this.dynamicArraySize = arrayType.getDynamicSizeExpr();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLLocalVariable#allocate(org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void allocate(ILLCodeTarget target, LLOperand value) {
		if (dynamicArraySize != null) {
			try {
				arraySizeOp = target.getGenerator().generateTypedExpr(dynamicArraySize);
			} catch (ASTException e) {
				target.getGenerator().recordError(e);
			}
		}
		if (arraySizeOp == null) {
			super.allocate(target, value);
		} else {
			LLSymbolOp tempOp = new LLSymbolOp(addrSymbol);
			LLIntType int32Type = target.getGenerator().getTypeEngine().getIntType(32);
			LLOperand i32Size;
			try {
				i32Size = target.getGenerator().generateCast(dynamicArraySize, int32Type, dynamicArraySize.getType(), arraySizeOp);
			} catch (ASTException e) {
				target.getGenerator().recordError(e);
				return;
			}
			target.emit(new LLAllocaInstr(tempOp, arrayType.getSubType(), i32Size));
					//new LLTypeIdxOp(int32Type, i32Size)));
			if (value != null)
				target.emit(new LLStoreInstr(symbol.getType(), value, tempOp));
		}
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLLocalVariable#deallocate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public void deallocate(ILLCodeTarget target) {
		super.deallocate(target);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLLocalVariable#load(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand load(ILLCodeTarget target) {
		if (dynamicArraySize == null)
			return super.load(target);
		
		LLOperand symbolOp = new LLSymbolOp(addrSymbol);
		LLTempOp arrayPtr = target.newTemp(symbol.getType());
		TypeEngine typeEngine = target.getGenerator().getTypeEngine();
		/*
		target.emit(new LLCastInstr(castedAddr, ECast.BITCAST,
				typeEngine.getPointerType(arrayType.getSubType()), 
				symbolOp, arrayType));
				*/
		target.emit(new LLGetElementPtrInstr(arrayPtr, 
				typeEngine.getPointerType(arrayType.getSubType()),
				symbolOp, new LLConstOp(0)));
		LLTempOp castedAddr = target.newTemp(symbol.getType());
		target.emit(new LLCastInstr(castedAddr, ECast.BITCAST,
				typeEngine.getPointerType(arrayType.getSubType()), 
				arrayPtr,
				typeEngine.getPointerType(symbol.getType())
		));
		LLTempOp temp = target.newTemp(symbol.getType());
		target.emit(new LLLoadInstr(temp, symbol.getType(), castedAddr));
		return temp;
	}
}
