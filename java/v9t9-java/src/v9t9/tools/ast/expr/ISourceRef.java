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
	int getOffset();
	/** 1... */
	int getLine();
	/** 1... */
	int getColumn();
}
