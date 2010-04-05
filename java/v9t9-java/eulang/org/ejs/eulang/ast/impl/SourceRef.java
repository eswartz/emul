/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ISourceRef;

/**
 * @author ejs
 *
 */
public class SourceRef implements ISourceRef {
	private final String file;
	private final int line;
	private final int column;
	private final int offs;
	private final int length;

	public SourceRef(String file, int offs, int length, int line, int column) {
		this.file = file;
		this.offs = offs;
		this.length = length;
		this.line = line;
		this.column = column;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return file + ":" + line + ":" +column;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getColumn()
	 */
	@Override
	public int getColumn() {
		return column;
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
		return line;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getOffset()
	 */
	@Override
	public int getOffset() {
		return offs;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.ISourceRef#getLength()
	 */
	@Override
	public int getLength() {
		return length;
	}
}
