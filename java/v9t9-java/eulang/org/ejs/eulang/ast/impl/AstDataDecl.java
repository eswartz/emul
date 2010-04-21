/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstDataDecl;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLStaticField;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstDataDecl extends AstTypedExpr implements IAstDataDecl {

	private IAstNodeList<IAstTypedNode> statics;
	private IAstNodeList<IAstTypedNode> ifields;


	protected IScope scope;

		
	public AstDataDecl(IAstNodeList<IAstTypedNode> fields,
			IAstNodeList<IAstTypedNode> statics, IScope scope) {
		this.scope = scope;
		scope.setOwner(this);
		setFields(fields);
		setStatics(statics);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstDataDecl copy(IAstNode copyParent) {
		AstDataDecl copied = fixup(this, new AstDataDecl(doCopy(ifields, copyParent), doCopy(statics, copyParent),
				getScope().newInstance(getCopyScope(copyParent))));
		remapScope(getScope(), copied.getScope(), copied);
		return copied;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ifields == null) ? 0 : ifields.hashCode());
		result = prime * result + ((statics == null) ? 0 : statics.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
		AstDataDecl other = (AstDataDecl) obj;
		if (ifields == null) {
			if (other.ifields != null)
				return false;
		} else if (!ifields.equals(other.ifields))
			return false;
		if (statics == null) {
			if (other.statics != null)
				return false;
		} else if (!statics.equals(other.statics))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}


	@Override
	public void setParent(IAstNode node) {
		super.setParent(node);

		if (node != null) {
			while (node != null) {
				if (node instanceof IAstScope) {
					scope.setParent(((IAstScope) node).getScope());
					break;
				}
				node = node.getParent();
			}
		} else {
			scope.setParent(null);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "DATA";
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#getFields()
	 */
	@Override
	public IAstNodeList<IAstTypedNode> getFields() {
		return ifields;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#getStatics()
	 */
	@Override
	public IAstNodeList<IAstTypedNode> getStatics() {
		return statics;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#setFields(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setFields(IAstNodeList<IAstTypedNode> fields) {
		this.ifields = reparent(this.ifields, fields);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#setStatics(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStatics(IAstNodeList<IAstTypedNode> statics) {
		this.statics = reparent(this.statics, statics);

	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { ifields, statics };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (statics == existing) 
			setStatics((IAstNodeList<IAstTypedNode>) another);
		else if (ifields == existing)
			setFields((IAstNodeList<IAstTypedNode>) another);
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
		
		if (canReplaceType(this)) {
			LLDataType data = createDataType(typeEngine); 
			changed |= updateType(this, data);
		}
		return changed;
	}


	public LLDataType createDataType(TypeEngine typeEngine) {
		List<LLInstanceField> newIFields = new ArrayList<LLInstanceField>();
		
		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					LLType type = symbolExpr.getType();
					LLInstanceField field = new LLInstanceField(symbolExpr.getSymbol().getName(), type, symbolExpr, defaul);
					newIFields.add(field);
				}
			}
		}
		
		List<LLStaticField> newSFields = new ArrayList<LLStaticField>();
		for (IAstTypedNode node : statics.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					LLType type = symbolExpr.getType();
					String name = symbolExpr.getSymbol().getName();
					LLStaticField field = new LLStaticField(name, type, 
							symbolExpr.getSymbol(),
							symbolExpr, defaul);
					newSFields.add(field);
				}
			}
		}
		
		String name = null;
		if (getParent() instanceof IAstDefineStmt) {
			name = ((IAstDefineStmt) getParent()).getSymbol().getName();
		}
		LLDataType data = new LLDataType(typeEngine, name,
				(LLInstanceField[]) newIFields.toArray(new LLInstanceField[newIFields.size()]),
				(LLStaticField[]) newSFields.toArray(new LLStaticField[newSFields.size()]));
		typeEngine.register(data);
		return data;
	}
	
}
