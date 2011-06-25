/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.*;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.*;
import org.ejs.eulang.ast.*;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.*;

/**
 * @author ejs
 *
 */
public class AstInitListExpr extends AstInitNodeExpr implements IAstInitListExpr { 
	private IAstNodeList<IAstInitNodeExpr> initExprs;

	public AstInitListExpr(IAstTypedExpr context, IAstNodeList<IAstInitNodeExpr> initExprs) {
		super(context, null);
		setInitExprs(initExprs);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INITLIST");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInitListExpr copy() {
		return fixup(this,new AstInitListExpr(doCopy(getContext()), doCopy(initExprs)));
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((initExprs == null) ? 0 : initExprs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstInitListExpr other = (AstInitListExpr) obj;
		if (initExprs == null) {
			if (other.initExprs != null)
				return false;
		} else if (!initExprs.equals(other.initExprs))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#getInitExprs()
	 */
	@Override
	public IAstNodeList<IAstInitNodeExpr> getInitExprs() {
		return initExprs;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#setInitExprs(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setInitExprs(IAstNodeList<IAstInitNodeExpr> exprs) {
		this.initExprs = reparent(this.initExprs, exprs);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (getContext() != null)
			return new IAstNode[] { getContext(), initExprs };
		else
			return new IAstNode[] { initExprs };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == initExprs)
			setInitExprs((IAstNodeList<IAstInitNodeExpr>) another);
		else
			super.replaceChild(existing, another);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		// Types are driven top-down.  Force element types to the aggregate type.
		if (canInferTypeFrom(this) && getType().isComplete()) {
			for (int i = 0; i < initExprs.nodeCount(); i++) {
				IAstInitNodeExpr expr = initExprs.list().get(i);
				LLType exprType = typeEngine.getRealType(getType());
				if (exprType != getType()) {
					changed = true;
					setType(exprType);
				}
				Pair<Integer, LLType> info = expr.getInitFieldInfo(getType());
				LLType fieldType = info.second;
				assert fieldType != null;
				
				if (!fieldType.equals(expr.getType())) {
					// gaaaar... deal with our automagic casting to array types (see below) but don't
					// thwart cases where we actually know the type.
					if (expr.getType() != null && expr.getExpr() != null &&
							((expr.getType().getBasicType().getClassMask() & fieldType.getBasicType().getClassMask()) == 0
							|| ((expr.getType().getBasicType().getClassMask() | fieldType.getBasicType().getClassMask()) & LLType.TYPECLASS_DATA + LLType.TYPECLASS_MEMORY) != 0)) {
						expr.getExpr().setType(fieldType);
					} else {
						expr.replaceChild(expr.getExpr(), createCastOn(typeEngine, expr.getExpr(), fieldType));
					}
					expr.setType(fieldType);
					changed = true;
				}
			}
		}
		else if (getType() == null || getType() instanceof LLUnknownAggregateType || getType().isGeneric()) {
			// no idea -- make one
			LLType commonType = null;
			boolean hasStructOps = false;
			boolean hasIndexOps = false;
			
			for (int i = 0; i < initExprs.nodeCount(); i++) {
				IAstInitNodeExpr expr = initExprs.list().get(i);
				if (expr.getContext() instanceof IAstFieldExpr) {
					hasStructOps = true;
					break;
				}
				if (expr.getContext() instanceof IAstInitIndexExpr) {
					hasIndexOps = true;
					// no break
				}
				LLType fieldType = expr.getType();
				if (fieldType != null) {
					if (commonType == null) {
						commonType = fieldType;
					} else {
						commonType = typeEngine.getPromotionType(commonType, fieldType);
					}
				}
			}
			
			if (!hasIndexOps && !hasStructOps) {

				boolean madeInstance = false; 
				if (getType() instanceof LLDataType && getType().isGeneric()) {
					// TODO: smarter behavior
					int genericArgCount = getType().getCount();
					
					IAstNode genNode = null;
					ISymbol genSym = null;
					LLType genType = getType();
					if (genType instanceof LLGenericType) {
						genSym = ((LLGenericType) genType).getSymbol(); 
					}
					if (genType instanceof LLDataType) {
						genSym = ((LLDataType)genType).getSymbol();
					}
					if (genSym != null) {
						genNode = genSym.getDefinition();
						if (genNode instanceof IAstDefineStmt) {
							genericArgCount = ((IAstDefineStmt) genNode).getGenericVariables().length;
						}
					}
					
					IAstNodeList<IAstTypedExpr> typeVars = new AstNodeList<IAstTypedExpr>(IAstTypedExpr.class);
					for (IAstTypedExpr initExpr : initExprs.list()) {
						typeVars.add(new AstType(initExpr.getType()));
					}
					while (typeVars.nodeCount() < genericArgCount) {
						typeVars.add(new AstNilLitExpr("0", getType().getType(typeVars.nodeCount())));
					}
					IAstInstanceExpr instance = new AstInstanceExpr(new AstSymbolExpr(false, ((LLDataType)getType()).getSymbol()), 
							typeVars, getType());
					instance.setSourceRefTree(this.getSourceRef());
					ExpandAST expand = new ExpandAST(typeEngine, true);
					List<Message> messages = new ArrayList<Message>();
					IAstNode exp = expand.expand(messages, instance);
					if (exp != instance) {
						changed |= updateType(this, ((IAstTypedNode) exp).getType());
					}
					madeInstance = true;
				} 
				if (!madeInstance) {
					// unknown for now
					LLUnknownAggregateType unknown = new LLUnknownAggregateType();
					changed |= updateType(this, unknown);
				}
				if (changed)
					return changed;
			}
			if (commonType != null && !hasStructOps) {
				int maxIndex = 0;
				int curIndex = 0;
				for (int i = 0; i < initExprs.nodeCount(); i++) {
					IAstInitNodeExpr expr = initExprs.list().get(i);

					if (expr.getContext() instanceof IAstIntLitExpr) {
						curIndex = (int) ((IAstIntLitExpr) expr.getContext()).getValue();
					}
					if (!commonType.equals(expr.getType())) {
						expr.replaceChild(expr.getExpr(), createCastOn(typeEngine, expr.getExpr(), commonType));
						expr.setType(commonType);
						changed = true;
					}
					
					curIndex++;
					if (curIndex > maxIndex)
						maxIndex = curIndex;
				}
				
				LLArrayType arrayType = typeEngine.getArrayType(commonType, maxIndex, null);
				changed |= updateType(this, arrayType);
			}
		}
		return changed;
	}
}
