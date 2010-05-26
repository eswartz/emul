/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class ShiftOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param llvmName 
	 */
	public ShiftOperation(String name, String llvmName) {
		super(name, llvmName, false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.left != null && types.left.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires an integer left operand, got " + types.left.toString());
		if (types.right != null && types.right.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires an integer right operand, got " + types.right.toString());
		
		// now, prefer integers
		if (types.left == null) {
			types.left = typeEngine.INT;
		}
		if (types.right == null) {
			types.right = typeEngine.INT;
		}
		if (types.result == null) {
			types.result = types.left;
			if (types.result.getBits() == 0)
				types.result = typeEngine.INT;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType newLeft = typeEngine.getPromotionType(types.left, types.result);
		LLType newRight = typeEngine.getPromotionType(types.right, typeEngine.INT);
		if (newLeft == null || newRight == null)
			throw new TypeException("cannot convert result of '" + getName() + "' on " 
					+ types.left.toString() + " and " + types.right.toString() + " to " + types.result.toString());
		types.left = newLeft;
		types.right = newLeft;	// must be the same
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (types.left.getBasicType() != BasicType.INTEGRAL
				|| types.right.getBasicType() != BasicType.INTEGRAL
				|| types.result.getBasicType() != BasicType.INTEGRAL
				|| !types.result.equals(types.left)
				|| !types.right.equals(types.left)
				) {
			throw new TypeException("inconsistent types in expression");
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		LLOperand left = generator.generateTypedExpr(expr.getLeft());
		LLOperand right = generator.generateTypedExpr(expr.getRight());
		return generate(generator, currentTarget, expr, left, right);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.LLVMGenerator, org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.ast.IAstTypedExpr, org.ejs.eulang.llvm.ops.LLOperand, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator,
			ILLCodeTarget currentTarget, IAstTypedExpr expr, LLOperand left,
			LLOperand right) throws ASTException {

		String instr = this.getLLVMName();
		LLOperand ret = null;
		if (instr != null) {
			ret = currentTarget.newTemp(expr.getType());
			currentTarget.emit(new LLBinaryInstr(instr, ret, left.getType(), left, right));
		} else if (this == IBinaryOperation.SRC || this == IBinaryOperation.SLC) {
			ISymbol intrinsicSym = currentTarget.getTarget().getIntrinsic(
					currentTarget, 
					this == IBinaryOperation.SRC ? ITarget.Intrinsic.SHIFT_RIGHT_CIRCULAR : ITarget.Intrinsic.SHIFT_LEFT_CIRCULAR, 
					left.getType());
			
			LLCodeType intrinsicFuncType = (LLCodeType) intrinsicSym.getType();
			left = generator.generateCast(expr, intrinsicFuncType.getArgTypes()[0], left.getType(), left);
			right = generator.generateCast(expr, intrinsicFuncType.getArgTypes()[1], right.getType(), right);

			ret = currentTarget.newTemp(expr.getType());
			currentTarget.emit(new LLCallInstr(ret, left.getType(), 
					new LLSymbolOp(intrinsicSym), intrinsicFuncType,
					left, right));
		} else {
			generator.unhandled(expr);
			left = new LLConstOp(0);
		}
		return ret;
	}

}
