/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Mar 4, 2006
 *
 */
package v9t9.tools.ast.expr.impl;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IAstRegisterExpression;

public class AstRegisterExpression extends AstExpression implements
        IAstRegisterExpression {

    private int reg;
    private int wp;

    public AstRegisterExpression(int wp, int reg) {
        setWorkspacePointer(wp);
        setRegister(reg);
        dirty = false;
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
    public IAstExpression simplify() {
        return this;
    }

    public boolean equalValue(IAstExpression expr) {
        return expr instanceof IAstRegisterExpression
        && ((IAstRegisterExpression) expr).getRegister() == reg;
    }

    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }

    public IAstNode[] getReferencedNodes() {
        return NO_CHILDREN;
    }

}
