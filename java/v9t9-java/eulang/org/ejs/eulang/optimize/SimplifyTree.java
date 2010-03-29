/**
 * 
 */
package org.ejs.eulang.optimize;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;

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
	

	public IAstNode simplify(boolean[] anyChanged, IAstNode node) {

		boolean changed = false;
		IAstNode[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			IAstNode kid = children[i];
			IAstNode newkid = simplify(anyChanged, kid);
			if (newkid != null && newkid != kid) {
				newkid.setSourceRef(kid.getSourceRef());
				children[i] = newkid;
				changed = true;
				anyChanged[0] = true;
			}
		}
		
		if (changed) {
			node.replaceChildren(children);
		}
		
		if (!(node instanceof IAstTypedExpr)) 
			return node;
		
		IAstTypedExpr expr = (IAstTypedExpr) node;
		
		// change cast of literal to casted literal
		IAstNode simple = expr.simplify(typeEngine);
		
		return simple;
	}
	
}
