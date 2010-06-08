/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
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
			
			if (this == IOperation.SUB && commonType.getBasicType() == BasicType.POINTER && types.right.getBasicType() == BasicType.POINTER) {
				commonType = typeEngine.PTRDIFF;
			}
			if (types.result == null || types.result.isGeneric()) {
				types.result = commonType;
			}
		} 
		else if (types.left != null) {
			if (this == IOperation.ADD && types.left.getBasicType() == BasicType.POINTER)
				types.right = typeEngine.PTRDIFF;
			else
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
	public boolean transformExpr(IAstBinExpr expr, TypeEngine typeEngine, OpTypes types)
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
				return false;
			}
			throw new TypeException("cannot convert result of '" + getName() + "' on " 
					+ types.left.toString() + " and " + types.right.toString() + " to " + types.result.toString());
		}
		boolean changed = false;
		types.left = newLeft;
		types.right = newRight;
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
			if (this == IOperation.SUB) {
				if (right instanceof LLConstOp) {
					right = new LLConstOp(right.getType(), 0 - ((LLConstOp) right).getValue().longValue());
				} else {
					LLTempOp neg = currentTarget.newTemp(right.getType());
					currentTarget.emit(new LLBinaryInstr("sub", neg, neg.getType(), new LLConstOp(neg.getType(), 0), right));
					right = neg;
				}
			}
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
			
		} else if (this == IOperation.MOD) {
			//
			//	call %intrinsic.mod(i16, i16)
			//
			if (left.getType().getBasicType() == BasicType.FLOATING) {
				currentTarget.emit(new LLBinaryInstr("frem", ret, left.getType(), left, right));
				
			} else {
				ISymbol intrinsicSrc = currentTarget.getTarget().getIntrinsic(
						currentTarget, ITarget.Intrinsic.MODULO, left.getType());
				LLCodeType intrinsicFuncType = (LLCodeType) intrinsicSrc.getType();
				left = generator.generateCast(expr, intrinsicFuncType.getArgTypes()[0], left.getType(), left, false);
				right = generator.generateCast(expr, intrinsicFuncType.getArgTypes()[1], right.getType(), right, false);
				currentTarget.emit(new LLCallInstr(ret, left.getType(), 
						new LLSymbolOp(intrinsicSrc), intrinsicFuncType,
						left, right));
			}
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
			
			int bits = type.getBits();
			long limit = bits == 1 ? 1 : bits == 8 ? 0xff : bits == 16 ? 0xffff : bits == 32 ? 0xffffffff : Long.MAX_VALUE;
			
			long sl = (bits == 8) ? (byte) l : (bits == 16) ? (short) l : (bits == 32) ? (int) l : l;
			long sr = (bits == 8) ? (byte) r : (bits == 16) ? (short) r : (bits == 32) ? (int) r : r;
			
			long ul = (l < 0 ? (l + limit + 1) : l) & limit;
			long ur = (r < 0 ? (r + limit + 1) : r) & limit;

			l &= limit;
			r &= limit;
			
			if (this == IOperation.ADD) {
				value = sl + sr;
			} else if (this == IOperation.SUB) {
				value = sl - sr;
			} else if (this == IOperation.MUL) {
				value = ul * ur;
			} else if (this == IOperation.DIV) {
				if (r == 0)
					return null;
				value = sl / sr;
			} else if (this == IOperation.UDIV) {
				if (ur == 0)
					return null;
				value = ul / ur;
			} else if (this == IOperation.MOD) {
				if (ur == 0)
					return null;
				value = ul % ur;
			} else if (this == IOperation.REM) {
				if (r == 0)
					return null;
				value = l - (l / r) * r;
			} else if (this == IOperation.UREM) {
				if (ur == 0)
					return null;
				value = ul - (ul / ur) * ur;
			}
		}
		if (value != null)
			return new LLConstOp(type, value);
		return null;
	}
}
