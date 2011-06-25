/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public class ASTException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -667127808047937807L;
	private final IAstNode node;
	/**
	 * @param message
	 */
	public ASTException(IAstNode node, String message) {
		super(message);
		this.node = node;
	}
	public ASTException(IAstNode node, String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
		this.node = node;
	}
	
	/**
	 * @return the node
	 */
	public IAstNode getNode() {
		return node;
	}

}
