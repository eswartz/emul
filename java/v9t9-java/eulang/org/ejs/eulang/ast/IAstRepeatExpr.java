/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * A counted 'repeat' loop
 * @author ejs
 *
 */
public interface IAstRepeatExpr extends IAstTestBodyLoopExpr {
	 IAstRepeatExpr copy();
}
