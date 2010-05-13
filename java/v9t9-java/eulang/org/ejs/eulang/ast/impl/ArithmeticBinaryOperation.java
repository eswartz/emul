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
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class ArithmeticBinaryOperation extends Operation implements IBinaryOperation {

	private String intPrefix;
	private String floatPrefix;

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ArithmeticBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}

	/**
	 * @param string
	 * @param string2
	 * @param b
	 * @param string3
	 * @param string4
	 */
	public ArithmeticBinaryOperation(String name, String llvmName, boolean isCommutative, String intPrefix, String floatPrefix) {
		super(name, llvmName, isCommutative);
		this.intPrefix = intPrefix;
		this.floatPrefix = floatPrefix;
	}
	
	/**
	 * @return the intPrefix
	 */
	public String getIntPrefix() {
		return intPrefix;
	}
	/**
	 * @return the floatPrefix
	 */
	public String getFloatPrefix() {
		return floatPrefix;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		if (types.left != null && types.right != null) {
			LLType commonType;
			if (types.left.getBasicType() != types.right.getBasicType()
					|| (types.leftIsSymbol == types.rightIsSymbol))
				commonType = typeEngine.getPromotionType(types.left, types.right);
			else if (types.leftIsSymbol)
				commonType = types.left;
			else 
				commonType = types.right;
			if (commonType != null && commonType.getBits() == 0)
				commonType = typeEngine.INT;
			
			//commonType = typeEngine.getPromotionType(types.left, types.right);
			if (commonType == null)
				throw new TypeException("cannot find compatible type for '" + getName() + "' on " 
						+ types.left.toString() + " and " + types.right.toString());
			
			if (commonType.getBasicType() == BasicType.POINTER && this == IOperation.SUB) {
				commonType = typeEngine.PTRDIFF;
			}
			if (types.result == null || types.result.isGeneric()) {
				types.result = commonType;
			}
		} 
		else if (types.left != null) {
			types.right = types.left;
			types.result = types.left;
		}
		else if (types.right != null) {
			types.left = types.right;
			types.result = types.right;
		}
		else if (types.result != null) {
			types.left = types.result;
			types.right = types.result;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType newLeft;
		LLType newRight;
		if (types.left.getBasicType() != types.right.getBasicType()
				|| (types.leftIsSymbol == types.rightIsSymbol)) {
			newLeft = typeEngine.getPromotionType(types.left, types.result);
			newRight = typeEngine.getPromotionType(types.right, types.result);
		} else {
			newLeft = types.leftIsSymbol ? types.left : types.rightIsSymbol ? types.right : typeEngine.getPromotionType(types.left, types.result);
			newRight = types.rightIsSymbol ? types.right : types.leftIsSymbol ? types.left : typeEngine.getPromotionType(types.right, types.result);
		}
			
		if (newLeft == null || newRight == null) {
			if ((this == IOperation.ADD || this == IOperation.SUB) &&
					types.left.getBasicType() == BasicType.POINTER && types.right.getBasicType() == BasicType.INTEGRAL) {
				// fine
				return;
			}
			throw new TypeException("cannot convert result of '" + getName() + "' on " 
					+ types.left.toString() + " and " + types.right.toString() + " to " + types.result.toString());
		}
		types.left = newLeft;
		types.right = newRight;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if ((types.result.getBasicType().getClassMask() & LLType.TYPECLASS_PRIMITIVE) == 0) {
			// allow pointer math
			if ((this == IOperation.ADD || this == IOperation.SUB) &&
					types.left.getBasicType() == BasicType.POINTER && types.right.getBasicType() == BasicType.INTEGRAL)
				return;
			throw new TypeException("invalid type for '" + getName() + "' : " + types.result);
		}
		if (this == IOperation.SUB &&
				types.left.getBasicType() == BasicType.POINTER && types.right.getBasicType() == BasicType.POINTER) {
			if (types.result.getBasicType() == BasicType.INTEGRAL)
				return;
			throw new TypeException("inconsistent types in pointer difference expression");
		}
		if (!types.left.equals(types.right) 
				|| !types.result.equals(types.left)) {
			throw new TypeException("inconsistent types in expression");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		LLOperand left;
		left = generator.generateTypedExpr(expr.getLeft());
		LLOperand right;
		right = generator.generateTypedExpr(expr.getRight());
		
		return generate(generator, currentTarget, expr, left, right);
	}
	
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstTypedExpr expr, LLOperand left, LLOperand right) throws ASTException {
		LLType type = expr.getType();
		// pointer math
		if ((this == IOperation.ADD || this == IOperation.SUB) &&
				left.getType().getBasicType() == BasicType.POINTER && right.getType().getBasicType() == BasicType.INTEGRAL) {
			
			LLOperand ret = currentTarget.newTemp(type);
			currentTarget.emit(new LLGetElementPtrInstr(ret, ret.getType(), 
					left, right));
			return ret;
		}

		if (this == IOperation.SUB &&
				left.getType().getBasicType() == BasicType.POINTER && right.getType().getBasicType() == BasicType.POINTER) {
			// make into ints
			LLOperand tempLeft = currentTarget.newTemp(type);
			currentTarget.emit(new LLCastInstr(tempLeft, ECast.PTRTOINT, left.getType(), left, tempLeft.getType()));
			LLOperand tempRight = currentTarget.newTemp(type);
			currentTarget.emit(new LLCastInstr(tempRight, ECast.PTRTOINT, right.getType(), right, tempRight.getType()));
			LLOperand diff = currentTarget.newTemp(type);
			currentTarget.emit(new LLBinaryInstr(getLLVMName(), diff, type, tempLeft, tempRight));
			
			// now, scale down
			LLOperand ret = currentTarget.newTemp(type);
			int size = left.getType().getBits() / 8;
			currentTarget.emit(new LLBinaryInstr("sdiv exact", ret, type, diff, new LLConstOp(type, size)));
			return ret;
		}

		LLOperand ret = currentTarget.newTemp(type);
		
		String instr = this.getLLVMName();
		if (instr != null) {
			String prefix = (left.getType().getBasicType() == BasicType.FLOATING) ? 
					((ArithmeticBinaryOperation) this).getFloatPrefix() : ((ArithmeticBinaryOperation) this).getIntPrefix();
			if (prefix != null) 
				instr = prefix + instr;
			currentTarget.emit(new LLBinaryInstr(instr, ret, left.getType(), left, right));
			
		} else {
			generator.unhandled(null);
		}
		return ret;
	}
}
