/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.ast.IAstNode;

/**
 * @author ejs
 *
 */
public class TypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1115627835014641696L;
	private ITyped typed;

	public TypeException() {
		super();
	}

	public TypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeException(String message) {
		super(message);
	}

	public TypeException(Throwable cause) {
		super(cause);
	}

	public TypeException(ITyped node, String message) {
		super(message);
		this.typed = node;
	}
	
	/**
	 * @return the node
	 */
	public IAstNode getNode() {
		return typed != null ? typed.getNode() : null;
	}
}
