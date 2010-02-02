/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Mar 4, 2006
 *
 */
package v9t9.tools.decomp.expr;

/**
 * This references the value of the register.
 * @author ejs
 *
 */
public interface IAstRegisterExpression extends IAstExpression {
    public int getRegister();
    public void setRegister(int value);
    public int getWorkspacePointer();
    public void setWorkspacePointer(int wp);
}
