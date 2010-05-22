/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLBranchInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class BooleanComparisonBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param llvmName 
	 * @param isCommutative
	 */
	public BooleanComparisonBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.left != null && types.left.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an boolean left operand, got " + types.left.toString());
		if (types.right != null && types.right.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an boolean right operand, got " + types.right.toString());

		// now, prefer booleans
		if (types.left == null) {
			types.left = typeEngine.BOOL;
		}
		if (types.right == null) {
			types.right = typeEngine.BOOL;
		}
		if (types.result == null) {
			types.result = typeEngine.getPromotionType(types.left, types.right);
			if (types.result.getBits() == 0)
				types.result = typeEngine.BOOL;
		}
		
		if (types.left != null && types.right != null) {
			LLType commonType = typeEngine.getPromotionType(types.left, types.right);
			if (commonType == null)
				throw new TypeException("cannot find compatible type for comparing "
						+ types.left.toString() + " and " + types.right.toString());
		} 
		else if (types.left != null) {
			types.right = types.left;
		}
		else if (types.right != null) {
			types.left = types.right;
		}
		if (types.result != null) {
			if (types.result.getBasicType() != BasicType.BOOL)
				throw new TypeException("cannot store comparison result in " + types.result.toString());
		} else {
			types.result = typeEngine.BOOL;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType common = typeEngine.getPromotionType(types.left, types.right);
		if (common == null)
			throw new TypeException("cannot find compatible type for comparing "
					+ types.left.toString() + " and " + types.right.toString());
		types.left = types.right = common;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.left.equals(types.right) 
				|| types.result.getBasicType() != BasicType.BOOL) {
			throw new TypeException("inconsistent types in expression");
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		if (this == IOperation.COMPAND) {
			return generateShortCircuitAnd(generator, currentTarget, expr);
		} else if (this == IOperation.COMPOR) {
			return generateShortCircuitOr(generator, currentTarget,  expr);
		} else {
			generator.unhandled(expr);
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.LLVMGenerator, org.ejs.eulang.llvm.ILLCodeTarget, org.ejs.eulang.ast.IAstTypedExpr, org.ejs.eulang.llvm.ops.LLOperand, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator,
			ILLCodeTarget currentTarget, IAstTypedExpr expr, LLOperand left,
			LLOperand right) throws ASTException {
		generator.unhandled(expr);
		return null;
	}
	
	private LLOperand generateShortCircuitAnd(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		IBinaryOperation op = expr.getOp();
		assert op == IOperation.COMPAND;
		
		IScope scope = expr.getOwnerScope();
		
		// get a var for the outcome
		ISymbol boolResultSym = scope.addTemporary("and");
		boolResultSym.setType(expr.getType());
		LLOperand retval = new LLSymbolOp(boolResultSym);
		currentTarget.emit(new LLAllocaInstr(retval, expr.getType()));

		ISymbol rhsLabel, outLabel;

		///
		
		// calculate the left side and save that
		LLOperand left = generator.generateTypedExpr(expr.getLeft());
		currentTarget.store(expr.getType(), left, retval);
		
		rhsLabel = scope.addTemporary("rhsOut");
		rhsLabel.setType(generator.getTypeEngine().LABEL);
		outLabel = scope.addTemporary("andOut");
		outLabel.setType(generator.getTypeEngine().LABEL);
		
		// if it was false, done
		currentTarget.emit(new LLBranchInstr(
				expr.getLeft().getType(),
				//typeEngine.LLBOOL,
				left, new LLSymbolOp(rhsLabel), new LLSymbolOp(outLabel)));
		
		//
		
		// else, calculate rhs and overwrite the result with that
		currentTarget.addBlock(rhsLabel);
		
		LLOperand right = generator.generateTypedExpr(expr.getRight());
		
		currentTarget.store(expr.getRight().getType(), right, retval);
		currentTarget.emit(new LLUncondBranchInstr(new LLSymbolOp(outLabel)));
		
		currentTarget.addBlock(outLabel);
			
		LLOperand retTemp = currentTarget.newTemp(expr.getType());
		currentTarget.emit(new LLLoadInstr(retTemp, expr.getType(), retval));
		
		return retTemp;
	}
	
	private LLOperand generateShortCircuitOr(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		IBinaryOperation op = expr.getOp();
		assert op == IOperation.COMPOR;
		
		IScope scope = expr.getOwnerScope();
		
		// get result holder
		ISymbol boolResultSym = scope.addTemporary("or");
		boolResultSym.setType(expr.getType());
		LLOperand retval = new LLSymbolOp(boolResultSym);
		currentTarget.emit(new LLAllocaInstr(retval, expr.getType()));

		ISymbol rhsLabel, outLabel;

		///
		
		// calculate lhs
		LLOperand left = generator.generateTypedExpr(expr.getLeft());
		currentTarget.store(expr.getType(), left, retval);
		
		// if it was true, done
		rhsLabel = scope.addTemporary("rhsOut");
		rhsLabel.setType(generator.getTypeEngine().LABEL);
		outLabel = scope.addTemporary("andOut");
		outLabel.setType(generator.getTypeEngine().LABEL);
		currentTarget.emit(new LLBranchInstr(
				expr.getLeft().getType(),
				//typeEngine.LLBOOL,
				left, new LLSymbolOp(outLabel), new LLSymbolOp(rhsLabel)));
		
		//
		
		// else, see if the rhs is true
		currentTarget.addBlock(rhsLabel);
		
		LLOperand right = generator.generateTypedExpr(expr.getRight());
		
		currentTarget.store(expr.getRight().getType(), right, retval);
		currentTarget.emit(new LLUncondBranchInstr(new LLSymbolOp(outLabel)));
		
		currentTarget.addBlock(outLabel);
			
		LLOperand retTemp = currentTarget.newTemp(expr.getType());
		currentTarget.emit(new LLLoadInstr(retTemp, expr.getType(), retval));
		
		return retTemp;
	}
}
