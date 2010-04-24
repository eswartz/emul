/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Map;



/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstStmtScope {
	IAstModule copy(IAstNode copyParent);
	Map<String, String> getNonFileText();
}
