/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstCodeExpr extends AstTypedExpr implements IAstCodeExpr {

	private final IAstPrototype proto;
	private final IAstNodeList<IAstStmt> stmts;
	private final IScope scope;
	private final boolean macro;
	
	
	public AstCodeExpr(IAstPrototype proto, IScope scope, IAstNodeList<IAstStmt> stmts, boolean macro) {
		this.proto = proto;
		proto.setParent(this);
		this.scope = scope;
		this.macro = macro;
		scope.setOwner(this);
		this.stmts = stmts;
		if (stmts != null)
			stmts.setParent(this);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstCodeExpr copy(IAstNode copyParent) {
		IAstCodeExpr copied = new AstCodeExpr(doCopy(proto, copyParent), getScope().newInstance(getCopyScope(copyParent)), doCopy(stmts, copyParent), macro);
		remapScope(getScope(), copied.getScope(), copied);
		return fixup(this, copied);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(macro ? "macro" : "code");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#isMacro()
	 */
	@Override
	public boolean isMacro() {
		return macro;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getPrototype()
	 */
	@Override
	public IAstPrototype getPrototype() {
		return proto;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getStmts()
	 */
	@Override
	public IAstNodeList<IAstStmt> stmts() {
		return stmts;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (stmts != null)
			return new IAstNode[] { proto, stmts };
		else
			return new IAstNode[] { proto };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		throw new IllegalArgumentException();
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return expr.equals(this);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		// TODO Auto-generated method stub
		super.setType(type);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLCodeType newType = null;
		
		IAstStmt returns = stmts != null ? stmts.getLast() : null;
		
		LLCodeType protoType = getProtoType(typeEngine, returns);
		
		if (canInferTypeFrom(this) && getType().isComplete()) {
			newType = (LLCodeType) getType();
			if (returns instanceof ITyped 
					&& ((ITyped) returns).getType() != null
					&& ((ITyped) returns).getType().isMoreComplete(newType.getRetType())) {
				LLType[] types = newType.getTypes();
				types[0] = ((ITyped) returns).getType();
				newType = newType.updateTypes(types);
			}
		}
		if (newType == null || protoType.isMoreComplete(newType)) {
			/*if (proto.getType() != null && proto.getType().isComplete()) {
				newType = (LLCodeType) proto.getType();
			} else*/ {
				newType = protoType;
			}
		}
		
		boolean changed = false;
		if (proto.adaptToType(newType))
			changed = true;
		
		// see what the return statements do
		if (returns instanceof ITyped) {
			changed |= updateType((ITyped) returns, newType.getRetType());
		}
		
		changed |= updateType(this, newType);
		
		return changed;
	}

	private LLCodeType getProtoType(TypeEngine typeEngine, IAstStmt returns) {
		LLCodeType protoType = null;
		
		LLType[] infArgTypes = new LLType[proto.getArgCount()];
		int argIdx = 0;
		
		for (IAstArgDef arg : proto.argumentTypes()) {
			if (canInferTypeFrom(arg))
				infArgTypes[argIdx] = arg.getType();
			argIdx++;
		}
		
		LLType infRetType = proto.returnType().getType();
		if (infRetType == null || !infRetType.isComplete()) {
			// see what the return statements do
			if (returns instanceof ITyped) {
				if (canInferTypeFrom((ITyped) returns)) {
					infRetType = ((ITyped)returns).getType(); 
				}
			} else {
				infRetType = typeEngine.VOID;
			}
		}
		
		protoType = typeEngine.getCodeType(infRetType, infArgTypes);
		return protoType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		super.validateChildTypes(typeEngine);
		
		// see what the return statements do
		LLType thisType = ((IAstTypedNode) this).getType();
		if (thisType == null || !thisType.isComplete() || !(thisType instanceof LLCodeType))
			return;
		
		
		LLCodeType codeType = (LLCodeType) thisType;
		
		if (codeType.getRetType().getBasicType() == BasicType.VOID)
			return;
			
		IAstStmt returns = stmts != null ? stmts.getLast() : null;
		if (returns instanceof ITyped) {
			LLType kidType = ((ITyped) returns).getType();
			if (kidType != null && kidType.isComplete()) {
				if (!typeEngine.getBaseType(((LLCodeType) thisType).getRetType()).equals(
						typeEngine.getBaseType(kidType))) {
					throw new TypeException(returns, "code block does not return same type as prototype");
				}
			}
		}
	}

}
