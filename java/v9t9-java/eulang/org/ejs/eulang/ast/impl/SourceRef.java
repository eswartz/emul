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
	private final int length;
	private final int endLine;
	private final int endColumn;

	public SourceRef(String file, int length, int line, int column, int endLine, int endColumn) {
		this.file = file;
		this.length = length;
		this.line = Math.max(1, line);
		this.column = column;
		this.endLine = Math.max(1, endLine);
		this.endColumn = endColumn;
		
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + endColumn;
		result = prime * result + endLine;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + length;
		result = prime * result + line;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceRef other = (SourceRef) obj;
		if (column != other.column)
			return false;
		if (endColumn != other.endColumn)
			return false;
		if (endLine != other.endLine)
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (length != other.length)
			return false;
		if (line != other.line)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return file + ":" + line + ":" +column + " [" +getLength() + "]";
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
		return endColumn;
	}
	
	public int getEndLine() {
		return endLine;
		
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
			|| getEndLine() >= sourceRef.getEndLine()
			&& getEndColumn() >= sourceRef.getEndColumn());
	}
}
