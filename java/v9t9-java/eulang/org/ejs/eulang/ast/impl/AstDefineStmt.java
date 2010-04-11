/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstDefineStmt extends AstStatement implements IAstDefineStmt {

	private IAstSymbolExpr id;
	//private IAstTypedExpr expr;
	private Map<ISymbol, IAstTypedExpr> expansions = new HashMap<ISymbol, IAstTypedExpr>();
	private List<IAstTypedExpr> bodyList = new ArrayList<IAstTypedExpr>();
	//private Map<LLType, IAstTypedExpr> typedBodyMap = new HashMap<LLType, IAstTypedExpr>();
	
	private Map<LLType, List<IAstTypedExpr>> instanceMap = new HashMap<LLType, List<IAstTypedExpr>>();

	public AstDefineStmt(IAstSymbolExpr name, List<IAstTypedExpr> bodyList) {
		this.id = name;
		id.setParent(this);
		setSymbolExpr(name);
		for (IAstTypedExpr body : bodyList) {
			body.setParent(null);
			body.setParent(this);
			this.bodyList.add(body);
		}
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstDefineStmt copy(IAstNode copyParent) {
		// TODO: copy expansions
		List<IAstTypedExpr> bodyCopies = new ArrayList<IAstTypedExpr>();
		for (IAstTypedExpr body : bodyList) {
			bodyCopies.add((IAstTypedExpr) body.copy(null));
		}
		return fixup(this, new AstDefineStmt(doCopy(id, copyParent), bodyCopies));
	}

	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 777;
		result = prime * result
				+ ((bodyList == null) ? 0 : bodyList.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//if (!super.equals(obj))
		//	return false;
		if (getClass() != obj.getClass())
			return false;
		AstDefineStmt other = (AstDefineStmt) obj;
		if (bodyList == null) {
			if (other.bodyList != null)
				return false;
		} else if (!bodyList.equals(other.bodyList))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "DEFINE";
	}
	

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		IAstNode[] kids = new IAstNode[bodyList.size() + 1];
		kids[0] = id;
		System.arraycopy(bodyList.toArray(), 0, kids, 1, kids.length - 1);
		return kids;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		IAstNode[] kids = getChildren();
		if (expansions.size() > 0) {
			IAstNode[] allKids = new IAstNode[kids.length + expansions.size()];
			System.arraycopy(kids, 0, allKids, 0, kids.length);
			int idx = kids.length;
			for (IAstTypedExpr expansion : expansions.values())
				allKids[idx++] = expansion;
			return allKids;
		}
		return kids;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getSymbolExpr() == existing) {
			setSymbolExpr((IAstSymbolExpr) another);
		} else if (getExpr() == existing) {
			setExpr((IAstSymbolExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return bodyList.isEmpty() ? null : bodyList.get(0);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return id.getSymbol();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		//this.expr = reparent(this.expr, expr);
		if (bodyList.isEmpty()) 
			bodyList.add(expr);
		else
			bodyList.set(0, expr);
		expr.setParent(this);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setSymbolExpr(IAstSymbolExpr id) {
		Check.checkArg(id);
		this.id = reparent(this.id, id);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	/*
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		return inferTypesFromChildren(new ITyped[] { expr, id });
	}
	*/

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#expansions()
	 */
	/*
	@Override
	public Map<ISymbol, IAstTypedExpr> instances() {
		return expansions;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		// don't worry about symbol type
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#bodyList()
	 */
	@Override
	public List<IAstTypedExpr> bodyList() {
		return bodyList;
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#typedBodyMap()
	 */
	//@Override
	//public Map<LLType, IAstTypedExpr> typedBodyMap() {
	//	return typedBodyMap;
	//}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#instanceMap()
	 */
	@Override
	public Map<LLType, List<IAstTypedExpr>> bodyToInstanceMap() {
		return Collections.unmodifiableMap(instanceMap);
	}
	
	protected boolean typeMatchesExactly(LLType orig, LLType target) {
		if (orig == null || target == null)
			return false;
		if (orig.equals(target))
			return true;
		return false;
	}
	protected boolean typeMatchesCompatible(LLType orig, LLType target) {
		if (orig == null || target == null)
			return false;
		return orig.isMoreComplete(target);
	}
	protected boolean typeMatchesGeneric(LLType orig, LLType target) {
		if (orig == null || target == null)
			return true;
		return orig.isMoreComplete(target);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getMatchingExpr(org.ejs.eulang.types.LLType)
	 */
	@Override
	public IAstTypedExpr getMatchingBodyExpr(LLType type) {
		if (bodyList.isEmpty())
			return null;
		
		if (type == null) {
			// then the first
			return bodyList.get(0);
		}
		
		if (type.isComplete()) {
			// look for exact matches
			for (IAstTypedExpr expr : bodyList) {
				if (typeMatchesExactly(expr.getType(), type))
					return expr;
			}	
			// then compatible ones
			for (IAstTypedExpr expr : bodyList) {
				if (typeMatchesCompatible(expr.getType(), type))
					return expr;
			}	
		}
		// then generic matches
		for (IAstTypedExpr expr : bodyList) {
			if (typeMatchesGeneric(expr.getType(), type))
				return expr;
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getMatchingInstance(org.ejs.eulang.types.LLType)
	 */
	@Override
	public IAstTypedExpr getMatchingInstance(LLType type, LLType instanceType) {
		if (type == null) {
			return bodyList.get(0);
		}

		if (type.isComplete() && !type.isGeneric()) {
			assert type.equals(instanceType);
			return getMatchingBodyExpr(type);
		}
		
		List<IAstTypedExpr> list = instanceMap.get(type);
		if (list == null)
			return null;
		
		for (IAstTypedExpr expr : list) {
			if (typeMatchesExactly(expr.getType(), type))
				return expr;
		}
		/*
		for (IAstTypedExpr expr : list) {
			if (typeMatchesCompatible(expr.getType(), type))
				return expr;
		}
		for (IAstTypedExpr expr : list) {
			if (typeMatchesGeneric(expr.getType(), type))
				return expr;
		}
		*/
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#registerInstance(org.ejs.eulang.types.LLType, org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void registerInstance(LLType type, IAstTypedExpr expr) {
		if (type == null || !type.isGeneric()) {
			throw new IllegalArgumentException();
		}
		
		List<IAstTypedExpr> list = instanceMap.get(type);
		if (list == null) {
			for (IAstTypedExpr body : bodyList) {
				if (body.getType().equals(type)) {
					list = new ArrayList<IAstTypedExpr>();
					instanceMap.put(type, list);
					break;
				}
			}
			if (list == null)
				throw new IllegalArgumentException("type should match one inferred previously");
		}
		if (!list.contains(expr))
			list.add(expr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getConcreteInstances()
	 */
	@Override
	public Collection<IAstTypedExpr> getConcreteInstances() {
		List<IAstTypedExpr> list = new ArrayList<IAstTypedExpr>();
		for (IAstTypedExpr expr : bodyList) {
			if (expr.getType().isComplete() && !expr.getType().isGeneric())
				list.add(expr);
		}
		for (List<IAstTypedExpr> ilist : instanceMap.values()) {
			list.addAll(ilist);
		}
		return list;
	}
	
}
