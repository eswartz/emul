/**
 * 
 */
package org.ejs.eulang.types;

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
	private IAstNode node;

	public TypeException(IAstNode node, String string) {
		super(string);
		this.node = node;
	}

	/**
	 * @return the node
	 */
	public IAstNode getNode() {
		return node;
	}
	public TypeException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public TypeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TypeException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
