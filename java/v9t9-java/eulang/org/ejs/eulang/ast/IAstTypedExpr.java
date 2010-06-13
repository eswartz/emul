/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.TypeEngine;




/**
 * @author ejs
 *
 */
public interface IAstTypedExpr extends IAstTypedNode {
    /** 
     * Compare equality, value-wise.  This assumes the
     * expressions are of the same structure -- call
     * simplify() first if necessary.
     * 
     * @see #simplify()
     */
    public boolean equalValue(IAstTypedExpr expr);
}
