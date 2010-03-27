/**
 * 
 */
package org.ejs.eulang.types;

import java.util.List;

import org.ejs.eulang.ast.Error;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.Message;
import org.ejs.eulang.ast.TypeEngine;

/**
 * This class infers types in an AST.
 * <p>
 * First, any missing types are filled in from bottom up.  Some leaf nodes
 * may have types: {@link IAstLitExpr} has a known type.  {@link IAstSymbolExpr}
 * has a type if its symbol has one.  If these work, then parent expressions
 * are populated with the types.  Each node handles this itself.
 * 
 * @author ejs
 *
 */
public class TypeInference {

	/**
	 * Infer the types in the tree from known types.
	 */
	public void infer(List<Message> messages, TypeEngine typeEngine, IAstNode node) {
		int treeDepth = node.getDepth();
		int inferUps = 0;
		while (inferUp(messages, typeEngine, node) && inferUps <= treeDepth) 
			inferUps++;
		System.err.println("# up inference passes: " + inferUps);
	}

	/**
	 * Propagate known types down.
	 */
	public void propagateTypes(List<Message> messages, TypeEngine typeEngine, IAstNode node) {
		propagateTypesDown(messages, typeEngine, node);
	}
	
	/**
	 * @param messages
	 * @param typeEngine
	 * @param node
	 */
	private boolean inferUp(List<Message> messages, TypeEngine typeEngine,
			IAstNode node) {
		boolean changed = false;
		for (IAstNode kid : node.getChildren()) {
			changed |= inferUp(messages, typeEngine, kid);
		}
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typed = (IAstTypedNode) node;
			if (changed || typed.getType() == null) {
				try {
					LLType type = typed.inferTypeFromChildren(typeEngine);
					if (type != null) {
						typed.setType(type);
						changed = true;
					}
				} catch (TypeException e) {
					messages.add(new Error(node.getSourceRef(), e.getMessage()));
				}
			}
		}		
		return changed;
	}

	/**
	 * @param messages
	 * @param typeEngine
	 * @param node
	 */
	private void propagateTypesDown(List<Message> messages, TypeEngine typeEngine,
			IAstNode node) {
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typed = (IAstTypedNode) node;
			try {
				LLType type = typed.getType();
				if (type == null) {
					type = typed.inferTypeFromChildren(typeEngine);
				}
				if (type != null) {
					typed.setTypeOnChildren(typeEngine, type);
					typed.setType(type);
				} else {
					messages.add(new Error(node.getSourceRef(), "Could not infer types"));
				}
			} catch (TypeException e) {
				messages.add(new Error(node.getSourceRef(), e.getMessage()));
			}
		}
		for (IAstNode kid : node.getChildren()) {
			propagateTypesDown(messages, typeEngine, kid);
		}
	}

}
