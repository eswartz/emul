/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Collection;
import java.util.Set;


/**
 * @author ejs
 *
 */
public interface IAstCodeExpr extends IAstTypedExpr, IAstStmtScope {
	IAstCodeExpr copy();
	
	IAstPrototype getPrototype();
	
	boolean hasAttr(String attr);
	
	/** Tell whether the block was declared as a macro, meaning it should be substituted on use */
	boolean isMacro();
	/** Tell whether the block was declared as a method, meaning when referenced as a field, it should
	 * be converted */
	boolean isMethod();

	/**
	 * @return
	 */
	Collection<? extends String> getAttrs();
}
