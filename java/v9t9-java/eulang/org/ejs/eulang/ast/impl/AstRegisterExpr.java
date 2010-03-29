/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Mar 4, 2006
 *
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstRegisterExpr;

public class AstRegisterExpr extends AstExpr implements
        IAstRegisterExpr {

    private int reg;
    private int wp;

    public AstRegisterExpr(int wp, int reg) {
        setWorkspacePointer(wp);
        setRegister(reg);
        dirty = false;
    }
    
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 77;
		result = prime * result + reg;
		result = prime * result + wp;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstRegisterExpr other = (AstRegisterExpr) obj;
		if (reg != other.reg)
			return false;
		if (wp != other.wp)
			return false;
		return true;
	}



	public int getRegister() {
        return reg;
    }

    public void setRegister(int value) {
        org.ejs.coffee.core.utils.Check.checkArg((value >= 0 && value < 16));
        this.reg = value;
    }

    public int getWorkspacePointer() {
        return wp;
    }
    
    public void setWorkspacePointer(int wp) {
        org.ejs.coffee.core.utils.Check.checkArg((wp >= 0 && wp < 0x10000));
        if (this.wp != wp) {
			dirty = true;
		}
        this.wp = wp;
    }
    public IAstExpr simplify() {
        return this;
    }

    public boolean equalValue(IAstExpr expr) {
        return expr instanceof IAstRegisterExpr
        && ((IAstRegisterExpr) expr).getRegister() == reg;
    }

    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }
    @Override
	public void replaceChildren(IAstNode[] children) {
	}
	
}
