/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.symbols.IScope;


/**
 * @author ejs
 *
 */
public interface IAstScope extends IAstNode {
	/**
	 * @return
	 */
	IScope getScope(); 
}
