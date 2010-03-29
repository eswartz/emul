/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstReturnStmt;
import org.ejs.eulang.ast.IAstStatement;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstCodeExpr extends AstTypedExpr implements IAstCodeExpr {

	private final IAstPrototype proto;
	private final IAstNodeList<IAstStatement> stmts;
	private final IScope scope;
	private final boolean macro;
	
	
	public AstCodeExpr(IAstPrototype proto, IScope scope, IAstNodeList<IAstStatement> stmts, boolean macro) {
		this.proto = proto;
		proto.setParent(this);
		this.scope = scope;
		this.macro = macro;
		scope.setOwner(this);
		this.stmts = stmts;
		stmts.setParent(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return (macro ? "macro" : "code")+ ":" + getTypeString();
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
	public IAstNodeList<IAstStatement> stmts() {
		return stmts;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { proto, stmts };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpr expr) {
		return expr.equals(this);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLCodeType newType = null;
		
		if (proto.getType() != null && proto.getType().isComplete()) {
			newType = (LLCodeType) proto.getType();
		} else {
			LLType[] infArgTypes = new LLType[proto.getArgCount()];
			int argIdx = 0;
			
			for (IAstArgDef arg : proto.argumentTypes()) {
				infArgTypes[argIdx] = arg.getType();
				argIdx++;
			}
			
			LLType infRetType = proto.returnType().getType();
			if (infRetType == null || !infRetType.isComplete()) {
				// see what the return statements do
				infRetType = typeEngine.VOID;
				for (IAstReturnStmt returns : getReturnStmts()) {
					if (returns.getExpr() == null) {
						infRetType = typeEngine.VOID;
					} else if (canInferTypeFrom(returns)) {
						infRetType = returns.getType();		// take last
					}
				}
			}
			
			newType = typeEngine.getCodeType(infRetType, infArgTypes);
		}
		
		boolean changed = false;
		if (proto.adaptToType(newType))
			changed = true;
		
		// see what the return statements do
		for (IAstReturnStmt returns : getReturnStmts()) {
			if (returns.getExpr() == null && returns.getType() == null) {
				returns.setType(newType.getRetType());
				changed = true;
			}
			else if (canReplaceType(returns)) {
				returns.setType(newType.getRetType());
				changed = true;
			}
		}
		
		changed |= updateType(this, newType);
		
		return changed;
	}
	
	/**
	 * @return
	 */
	private List<IAstReturnStmt> getReturnStmts() {
		List<IAstReturnStmt> list = null;
		for (IAstNode node : stmts.list()) {
			if (node instanceof IAstReturnStmt) {
				if (list == null)
					list = new ArrayList<IAstReturnStmt>();
				list.add((IAstReturnStmt) node);
			}
		}
		return list != null ? list : Collections.<IAstReturnStmt> emptyList();
	}


}
