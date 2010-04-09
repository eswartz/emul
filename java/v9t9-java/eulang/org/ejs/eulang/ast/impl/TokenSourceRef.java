/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.antlr.runtime.Token;
import org.ejs.eulang.ISourceRef;


/**
 * @author ejs
 *
 */
public class TokenSourceRef implements ISourceRef {
	private final Token token;
	private final String file;
	private final int length;

	public TokenSourceRef(String file, Token token, int length) {
		this.file = file;
		this.token = token;
		this.length = length;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFile() + ":" + getLine() + ":" + getColumn() + " [" + getLength() + "]";
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
		return Math.max(1, token.getLine());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getLength()
	 */
	@Override
	public int getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ISourceRef#getEndColumn()
	 */
	@Override
	public int getEndColumn() {
		return getColumn() + getLength();
	}
	
	public int getEndLine() {
		return getLine();
		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ISourceRef#contains(org.ejs.eulang.ISourceRef)
	 */
	@Override
	public boolean contains(ISourceRef sourceRef) {
		return getFile().equals(sourceRef.getFile())
		&& (getLine() < sourceRef.getLine()
		|| (getLine() == sourceRef.getLine())
			&& getColumn() <= sourceRef.getColumn())
		&& (getEndLine() > sourceRef.getEndLine()
			|| (getEndLine() == sourceRef.getEndLine()
			&& getEndColumn() >= sourceRef.getEndColumn()));
	}
}
