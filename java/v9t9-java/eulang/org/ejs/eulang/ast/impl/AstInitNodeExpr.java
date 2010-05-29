/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstInitNodeExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.BaseLLField;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstInitNodeExpr extends AstTypedExpr implements IAstInitNodeExpr { 
	private IAstTypedExpr context;
	private IAstTypedExpr expr;

	public AstInitNodeExpr(IAstTypedExpr context, IAstTypedExpr expr) {
		setContext(context);
		setExpr(expr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INIT");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInitNodeExpr copy() {
		return fixup(this,new AstInitNodeExpr(doCopy(context), doCopy(expr)));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#getInitExprs()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#setInitExprs(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitNodeExpr#getContext()
	 */
	@Override
	public IAstTypedExpr getContext() {
		return context;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitNodeExpr#setContext(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setContext(IAstTypedExpr context) {
		this.context = reparent(this.context, context);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (context != null)
			return new IAstNode[] { context, expr };
		else
			return new IAstNode[] { expr };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == context)
			setContext((IAstTypedExpr) another);
		else if (existing == expr)
			setExpr((IAstTypedExpr) another);
		else
			throw new IllegalArgumentException();
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		if (canReplaceType(context))
			changed |= updateType(context, expr.getType());
		changed |= updateType(this, expr.getType());
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitNodeExpr#getInitFieldIndex(org.ejs.eulang.types.LLType)
	 */
	@Override
	public Pair<Integer, LLType> getInitFieldInfo(LLType exprType) throws TypeException {
		IAstInitNodeExpr expr = this;
		int fieldIdx = 0;
		LLType fieldType;
		
		if (exprType instanceof LLAggregateType) {
			LLAggregateType aggType = (LLAggregateType) exprType;
			if (context instanceof IAstFieldExpr) {
				String fieldName = ((IAstFieldExpr)context).getField().getName();
				if (!(aggType instanceof LLDataType)) {
					throw new TypeException(expr, "cannot set field '" + fieldName + "' in non-data type " + aggType.getName());
				}
				LLDataType dataType = (LLDataType) aggType;
				BaseLLField field = dataType.getField(fieldName);
				if (field == null)
					throw new TypeException(expr, "no such field '" + fieldName + "' in type " + dataType.getName());
				fieldIdx = dataType.getFieldIndex(field);
			} 
			else if (context instanceof IAstIndexExpr) {
				fieldIdx = getIndex((IAstIndexExpr) context, aggType);
			}
			else if (context instanceof IAstIntLitExpr) {
				fieldIdx = (int) ((IAstIntLitExpr) context).getValue();
			} else {
				throw new TypeException(context, "unhandled initializer context");
			}
			if (fieldIdx >= aggType.getCount() || fieldIdx < 0)
				throw new TypeException(expr, "invalid array initializer index " + fieldIdx);
			fieldType = aggType.getType(fieldIdx);
		} else if (exprType instanceof LLArrayType) {
			if (context instanceof IAstFieldExpr) {
				String fieldName = ((IAstFieldExpr)context).getField().getName();
				throw new TypeException(expr, "cannot set field '" + fieldName + "' in non-data type " + exprType.getName());
			} 
			else if (context instanceof IAstIndexExpr) {
				fieldIdx = getIndex((IAstIndexExpr) context, exprType);
			}
			else if (context instanceof IAstIntLitExpr) {
				fieldIdx = (int) ((IAstIntLitExpr) context).getValue();
			} else {
				throw new TypeException(context, "unhandled initializer context");
			}
			if (fieldIdx < 0 || !((LLArrayType) exprType).isValidArrayIndex(fieldIdx)) 
				throw new TypeException(expr, "invalid array initializer index " + fieldIdx);
			fieldType = exprType.getSubType();
		} else {
			if (context instanceof IAstIntLitExpr) {
				fieldIdx = (int) ((IAstIntLitExpr) context).getValue();
			} else  if (context instanceof IAstIndexExpr) {
				fieldIdx = getIndex((IAstIndexExpr) context, expr.getType());
			} else if (context instanceof IAstFieldExpr) {
				throw new TypeException(context, "cannot initialize field in scalar " + expr.getType().getName());
			} else {
				throw new TypeException(context, "unhandled initializer context");
			}
			if (fieldIdx != 0) 
				throw new TypeException(expr, "can only initialize single element in scalar " + expr.getType().getName());
			fieldType = exprType;
		}
		return new Pair<Integer, LLType>(fieldIdx, fieldType);
	}
	

	private int getIndex(IAstIndexExpr context, LLType type)
			throws TypeException {
		int fieldIdx;
		IAstTypedExpr indexExpr = context.getIndex();
		if (!(indexExpr instanceof IAstIntLitExpr))
			throw new TypeException(context, "cannot initialize non-integral index " + indexExpr + "' in " + type.getName());
		fieldIdx = (int) ((IAstIntLitExpr) indexExpr).getValue();
		return fieldIdx;
	}
	
}
