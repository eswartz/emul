/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Mar 4, 2006
 *
 */
package org.ejs.eulang.ast;

/**
 * This references the value of the register.
 * @author ejs
 *
 */
public interface IAstRegisterExpr extends IAstExpr {
    public int getRegister();
    public void setRegister(int value);
    public int getWorkspacePointer();
    public void setWorkspacePointer(int wp);
}
