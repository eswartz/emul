/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class IndexBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public IndexBinaryOperation(String name) {
		super(name, null, false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		if (types.right == null) {
			types.right = typeEngine.INT;
		}
		if (types.left != null) {
			if (types.left instanceof LLArrayType || types.left instanceof LLPointerType) {
				types.result = types.left.getSubType();
			} else if (typeEngine.isStringType(types.left)) { // temporary
				types.result = typeEngine.CHAR;
			}
		} 
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public boolean transformExpr(IAstBinExpr expr, TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		// is this a string type?
		LLType sourceType = types.left;
		if (typeEngine.isStringType(sourceType)) {
			// if so, promote str[] to str.s[]
			LLDataType strType = ((LLDataType) types.left);
			LLInstanceField field = strType.getInstanceFields()[1];
			
			expr.getLeft().setParent(null);
			expr.getRight().setParent(null);
			
			IAstFieldExpr fieldExpr = new AstFieldExpr(expr.getLeft(), new AstName(field.getName()));
			fieldExpr.setType(field.getType());
			
			IAstTypedExpr indexExpr = new AstBinExpr(IOperation.INDEX, new AstDerefExpr(fieldExpr, false), expr.getRight());
			
			indexExpr.setType(fieldExpr.getType().getSubType());
			indexExpr = AstTypedNode.createCastOn(typeEngine, indexExpr, types.result); 
			
			indexExpr.setSourceRefTree(expr.getSourceRef());
			expr.getParent().replaceChild(expr, indexExpr);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		
		if (types.left instanceof LLArrayType || types.left instanceof LLPointerType) {
			if (!types.result.matchesExactly(types.left.getSubType())) {
				throw new TypeException("inconsistent types in index expression: " + types.result + " vs " + types.left.getSubType());
			}
		} else if (!types.left.isGeneric()) {
			throw new TypeException("cannot perform indexing on type " + types.left.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#generate(org.ejs.eulang.llvm.ILLCodeTarget)
	 */
	@Override
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException {
		LLOperand left;
		left = generator.generateTypedExprAddr(expr.getLeft());
		LLOperand right;
		right = generator.generateTypedExpr(expr.getRight());
		
		return generate(generator, currentTarget, expr, left, right);
	}
	
	public LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstTypedExpr expr, LLOperand array, LLOperand index) throws ASTException {

		// point to the array
		LLType arrayType = ((IAstBinExpr) expr).getLeft().getType();
		
		boolean isAlloca = false;
		if (arrayType instanceof LLArrayType && ((LLArrayType) arrayType).getDynamicSizeExpr() != null) {
			// convert to bare pointer
			arrayType = arrayType.getSubType();
			isAlloca = true;
		}
		
		LLType arrayPointerType = currentTarget.getTypeEngine().getPointerType(arrayType);
		// point to the element
		LLType elementType = currentTarget.getTypeEngine().getPointerType(expr.getType());

		array = generator.ensureAddressable(array);
		
		LLTempOp elPtr = currentTarget.newTemp(elementType);
		if (!isAlloca)
			currentTarget.emit(new LLGetElementPtrInstr(elPtr, arrayPointerType,
					array, new LLConstOp(0), index));
		else
			currentTarget.emit(new LLGetElementPtrInstr(elPtr, arrayPointerType,
					array, index));

		return elPtr;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#evaluate(org.ejs.eulang.types.LLType, org.ejs.eulang.ast.IAstLitExpr, org.ejs.eulang.ast.IAstLitExpr)
	 */
	@Override
	public LLConstOp evaluate(LLType type, IAstLitExpr litLeft,
			IAstLitExpr litRight) {
		return null;
	}
}
