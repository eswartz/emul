/**
 * 
 */
package org.ejs.eulang.ast;

import org.antlr.runtime.Token;

import v9t9.tools.ast.expr.ISourceRef;

/**
 * @author ejs
 *
 */
public class TokenSourceRef implements ISourceRef {
	private final Token token;
	private final String file;

	public TokenSourceRef(String file, Token token) {
		this.file = file;
		this.token = token;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFile() + ":" + getLine() + ":" + getColumn();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getColumn()
	 */
	@Override
	public int getColumn() {
		return token.getCharPositionInLine() + 1;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getFile()
	 */
	@Override
	public String getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getLine()
	 */
	@Override
	public int getLine() {
		return token.getLine();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getOffset()
	 */
	@Override
	public int getOffset() {
		return token.getCharPositionInLine();
	}

}
