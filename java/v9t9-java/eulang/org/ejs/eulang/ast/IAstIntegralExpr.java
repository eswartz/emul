/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * A literal value
 * 
 * @author eswartz
 *
 */
public interface IAstIntegralExpr extends IAstExpr {
    /** Get the value (never null) */
    public int getValue();
    
    /** Set the value (cannot be null) 
     *
     * @throws IllegalArgumentException if value is not valid for kind
     */
    public void setValue(int value);
    
}
