/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.ILLCodeVisitor;

/**
 * @author ejs
 *
 */
public abstract class LLBaseDirective {
	abstract public String toString();

	/**
	 * @param visitor
	 */
	public void accept(ILLCodeVisitor visitor) {
		if (visitor.enterDirective(this)) {
		}
		visitor.exitDirective(this);

	}
	
	
}
