/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtScope;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLStaticField;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstDataType extends AstStmtScope implements IAstDataType {

	private IAstNodeList<IAstTypedNode> statics;
	private IAstNodeList<IAstTypedNode> ifields;
	private ISymbol typeName;

		
	public AstDataType( ISymbol typeName,
			IAstNodeList<IAstStmt> stmts,
			IAstNodeList<IAstTypedNode> fields,
			IAstNodeList<IAstTypedNode> statics, IScope scope) {
		super(stmts, scope);
		this.typeName = typeName;
		setFields(fields);
		setStatics(statics);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstDataType copy(IAstNode copyParent) {
		return (IAstDataType) fixupStmtScope(new AstDataType(
				typeName,
				doCopy(stmtList, copyParent),
				doCopy(ifields, copyParent), doCopy(statics, copyParent),
				getScope().newInstance(getCopyScope(copyParent))));
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ifields == null) ? 0 : ifields.hashCode());
		result = prime * result + ((statics == null) ? 0 : statics.hashCode());
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
		AstDataType other = (AstDataType) obj;
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
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("DATA");
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
		return new IAstNode[] { ifields, statics, stmtList };
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
			super.replaceChild(existing, another);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		boolean changed = false;
		
		if (canReplaceType(this) || !getType().isComplete()) {
			LLDataType data = createDataType(typeEngine); 
			// TODO: references to this type's name need to be updated
			changed |= updateType(this, data);
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {

		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					LLType type = symbolExpr.getType();
					if (type instanceof LLCodeType)
						throw new TypeException(node, "cannot directly embed code in field");
				}
			}
		}
		
		for (IAstTypedNode node : statics.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					LLType type = symbolExpr.getType();
					if (type instanceof LLCodeType)
						throw new TypeException(node, "cannot directly embed code in field");
				}
			}
		}
	}

	public LLDataType createDataType(TypeEngine typeEngine) {
		List<LLInstanceField> newIFields = new ArrayList<LLInstanceField>();
		
		LLType[] existing = type != null ? ((LLDataType) type).getTypes() : null;
		int idx = 0;
		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					LLType type = symbolExpr.getType();
					if (type == null && existing != null)
						type = existing[idx]; 
					LLInstanceField field = new LLInstanceField(symbolExpr.getSymbol().getName(), type, symbolExpr, defaul);
					newIFields.add(field);
					idx++;
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
					if (type == null && existing != null)
						type = existing[idx]; 
					String name = symbolExpr.getSymbol().getName();
					LLStaticField field = new LLStaticField(name, type, 
							symbolExpr.getSymbol(),
							symbolExpr, defaul);
					newSFields.add(field);
					idx++;
				}
			}
		}
		
		ISymbol sym = getTypeName();
		assert sym != null;
		LLDataType data = typeEngine.getDataType(sym, newIFields, newSFields);
		
		/*data = (LLDataType) data.substitute(typeEngine, 
				new LLUpType(sym, 0, data), 
				data);
		*/
		return data;
	}

	public ISymbol getTypeName() {
		return typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#setTypeName(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void setTypeName(ISymbol typeName) {
		this.typeName = typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		super.setType(type);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstStmtScope#merge(org.ejs.eulang.ast.IAstStmtScope)
	 */
	@Override
	public void merge(IAstStmtScope added) throws ASTException {
		if (added instanceof IAstDataType) {
			doMerge(ifields, ((IAstDataType) added).getFields());
			doMerge(statics, ((IAstDataType) added).getStatics());
		} 
		super.merge(added);
	}
	
}
