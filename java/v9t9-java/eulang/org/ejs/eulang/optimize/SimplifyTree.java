/**
 * 
 */
package org.ejs.eulang.optimize;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAddrRefExpr;
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
		for (IAstNode kid : node.getChildren()) {
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
		if (!(node instanceof IAstTypedExpr)) 
			return null;
		
		IAstTypedExpr expr = (IAstTypedExpr) node;
		
		// change cast of literal to casted literal
		IAstNode simple = expr.simplify(typeEngine);
		if (simple != expr) {
			return simple;
		}
		
		return null;
	}
	
}
