/**
 * 
 */
package v9t9.tools.ast.expr;

/**
 * @author ejs
 *
 */
public interface ISourceRef {
	String getFile();
	/** file offset */
	int getOffset();
	/** number of characters */
	int getLength();
	/** 1... */
	int getLine();
	/** 1... */
	int getColumn();
}
