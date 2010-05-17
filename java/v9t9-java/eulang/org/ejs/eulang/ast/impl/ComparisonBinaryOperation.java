/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLCompareInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class ComparisonBinaryOperation extends Operation implements IBinaryOperation {

	private final String floatPrefix;
	private final String intPrefix;

	/**
	 * @param name
	 * @param llvmName 
	 * @param isCommutative
	 * @param isSignedOrdered TODO
	 */
	public ComparisonBinaryOperation(String name, String llvmName, boolean isCommutative, String intPrefix, String floatPrefix) {
		super(name, llvmName, isCommutative);
		this.intPrefix = intPrefix;
		this.floatPrefix = floatPrefix;
	}

	/**
	 * @return the intPrefix
	 */
	public String getLLIntPrefix() {
		return intPrefix;
	}
	/**
	 * @return the floatPrefix
	 */
	public String getLLFloatPrefix() {
		return floatPrefix;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
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
		LLType common;
		if (types.left.getBasicType() != types.right.getBasicType()
				|| (types.leftIsSymbol == types.rightIsSymbol)) {
			common = typeEngine.getPromotionType(types.left, types.right);
		} else if (types.leftIsSymbol) {
			common = types.left;
		} else if (types.rightIsSymbol) {
			common = types.right;
		} else {
			common = typeEngine.getPromotionType(types.left, types.right);
		}
		
		//common = typeEngine.getPromotionType(types.left, types.right);
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
			String cmp;
			if (left.getType().getBasicType() == BasicType.FLOATING) {
				cmp = getLLFloatPrefix()  + instr;
				instr = "fcmp"; 
			} else {
				cmp = getLLIntPrefix() + instr;
				instr = "icmp";
			}
			currentTarget.emit(new LLCompareInstr(instr, cmp, ret, left, right));
		} else {
			generator.unhandled(expr);
		}
		return ret;
	}
}
