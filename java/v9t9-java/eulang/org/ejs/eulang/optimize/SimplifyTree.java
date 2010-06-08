/**
 * 
 */
package org.ejs.eulang.optimize;

import java.util.Collection;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * Simplify the AST:
 * 
 * 1) Remove redundant cast operations
 * 2) Apply casts to literals
 * 3) Apply arithmetic to literals
 * 
 * @author ejs
 *
 */
public class SimplifyTree {

	private final TypeEngine typeEngine;
	
	public SimplifyTree(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		
	}
	

	public boolean simplify(IAstNode node) {

		boolean changed = false;
		IAstNode[] children;
		if (node instanceof IAstDefineStmt) {
			Collection<IAstTypedExpr> c = ((IAstDefineStmt) node).getConcreteInstances();
			children = (IAstNode[]) c.toArray(new IAstNode[c.size()]);
		}
		else
			children = node.getChildren();
		for (IAstNode kid : children) {
			changed |= simplify(kid);
		}
		
		IAstNode simple = doSimplify(node);
		if (simple != null) {
			node.getParent().replaceChild(node, simple);
			changed = true;
		}
		return changed;
	}

	private IAstNode doSimplify(IAstNode node) {
		// change cast of literal to casted literal
		IAstNode simple = node.simplify(typeEngine);
		if (simple != node) {
			return simple;
		}
		
		return null;
	}
	
}
