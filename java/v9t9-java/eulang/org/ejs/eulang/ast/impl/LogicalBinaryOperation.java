/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class LogicalBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param llvmName 
	 * @param isCommutative
	 */
	public LogicalBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.left != null && !types.left.isGeneric() 
				&& types.left.getBasicType() != BasicType.INTEGRAL
				&& types.left.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an integer or bool left operand, got " + types.left.toString());
		if (types.right != null && !types.right.isGeneric() 
				&& types.right.getBasicType() != BasicType.INTEGRAL
				&& types.right.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an integer right operand, got " + types.right.toString());

		// now, prefer integers
		if (types.left == null) {
			types.left = typeEngine.INT;
		}
		if (types.right == null) {
			types.right = typeEngine.INT;
		}
		if (types.result == null) {
			if (!types.leftIsSymbol && !types.rightIsSymbol)
				types.result = typeEngine.getPromotionType(types.left, types.right);
			else if (types.leftIsSymbol)
				types.result = types.left;
			else 
				types.result = types.right;
			if (types.result.getBits() == 0)
				types.result = typeEngine.INT;
		}

	}

	@Override
	public boolean transformExpr(IAstBinExpr expr, TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType newLeft = types.leftIsSymbol ? types.left : types.rightIsSymbol ? types.right : typeEngine.getPromotionType(types.left, types.result);
		LLType newRight = types.rightIsSymbol ? types.right : types.leftIsSymbol ? types.left : typeEngine.getPromotionType(types.right, types.result);
		if (newLeft == null || newRight == null)
			throw new TypeException("cannot convert result of '" + getName() + "' on " 
					+ types.left.toString() + " and " + types.right.toString() + " to " + types.result.toString());
		types.left = newLeft;
		types.right = newRight;
		boolean changed = false;
		changed |= expr.setLeft(AstTypedNode.createCastOn(typeEngine, expr.getLeft(), types.left));
		changed |= expr.setRight(AstTypedNode.createCastOn(typeEngine, expr.getRight(), types.right));
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.left.equals(types.right) 
				|| !types.result.equals(types.left)
				|| (types.result.getBasicType() != BasicType.INTEGRAL
						&& types.result.getBasicType() != BasicType.BOOL)) {
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
		LLOperand ret = currentTarget.newTemp(expr.getType());
		
		String instr = this.getLLVMName();
		if (instr != null) {
			currentTarget.emit(new LLBinaryInstr(instr, ret, left.getType(), left, right));
		} else {
			generator.unhandled(expr);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#evaluate(org.ejs.eulang.types.LLType, org.ejs.eulang.ast.IAstLitExpr, org.ejs.eulang.ast.IAstLitExpr)
	 */
	@Override
	public LLConstOp evaluate(LLType type, IAstLitExpr litLeft,
			IAstLitExpr litRight) {
		Number value = null;
		
		if (litLeft.getType().getBasicType() == BasicType.INTEGRAL
				&& litLeft instanceof IAstIntLitExpr
				&& litRight instanceof IAstIntLitExpr) {
			long l = ((IAstIntLitExpr) litLeft).getValue();
			long r = ((IAstIntLitExpr) litRight).getValue();
			
			if (this == IOperation.BITAND) {
				value = l & r;
			} else if (this == IOperation.BITOR) {
				value = l | r;
			} else if (this == IOperation.BITXOR) {
				value = l ^ r;
			}
		}
		if (value != null)
			return new LLConstOp(type, value);
		return null;
	}
}
