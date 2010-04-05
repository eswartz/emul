/**
 * 
 */
package org.ejs.eulang.types;

import java.util.List;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.Error;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedNode;

/**
 * This class infers types in an AST. We allow code to have unspecified types
 * for variables (in the prototype and in variable allocations) as well as
 * missing cast operators in expressions. By a consequence of the initial parse,
 * the temporary/oeprator nodes in the tree will be lacking types as well.
 * <p>
 * There are several pieces to inference:
 * <p>
 * <b>First</b>, we try to fill in any obvious missing types from bottom up,
 * using known type information. Some leaf nodes may have types:
 * {@link IAstLitExpr} has a known type. {@link IAstSymbolExpr} has a type if
 * its symbol has one. If these are set, then parent nodes can fill in the
 * appropriate types based on the semantics of operations.
 * <p>
 * "Appropriate" types may be fuzzy here; e.g., for adding two integers, rather
 * than filling in completely equal integer types, the addition node and
 * children may merely indicate "some integer is needed here".
 * <p>
 * Otherwise, the outcome of this phase is a tree where types may still be
 * incomplete, due to symbols that lack types.
 * <p>
 *  * <p>
 * <b>First</b>, if the existing types have incorrect basic type classes (e.g.
 * shifting by a float or adding a boolean), then these are due to user error,
 * and errors are thrown and the inference fails.

 * 
 * 
 * @author ejs
 * 
 */
public class TypeInference {

	/**
	 * Infer the types in the tree from known types.
	 */
	public boolean infer(List<Message> messages, TypeEngine typeEngine, IAstNode node) {
		return inferUp(messages, typeEngine, node); 
	}
	
	/**
	 * @param messages
	 * @param typeEngine
	 * @param node
	 */
	private boolean inferUp(List<Message> messages, TypeEngine typeEngine,
			IAstNode node) {
		
		boolean changed = false;
		
		// don't infer through defines
		//if (node instanceof IAstDefineStmt)
		//	return changed;
		
		for (IAstNode kid : node.getChildren()) {
			changed |= inferUp(messages, typeEngine, kid);
		}
		if (node instanceof ITyped) {
			IAstTypedNode typed = (IAstTypedNode) node;
			if (/*changed || typed.getType() == null || !typed.getType().isComplete()*/ true) {
				try {
					changed |= typed.inferTypeFromChildren(typeEngine);
				} catch (TypeException e) {
					messages.add(new Error(node, e.getMessage()));
				}
			}
		}		
		return changed;
	}

}
