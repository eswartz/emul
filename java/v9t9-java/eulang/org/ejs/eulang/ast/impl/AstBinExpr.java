/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IBinaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstBinExpr extends AstTypedExpr implements IAstBinExpr {

	private IAstTypedExpr right;
	private IAstTypedExpr left;
	private IBinaryOperation oper;

	public AstBinExpr(IBinaryOperation op, IAstTypedExpr left, IAstTypedExpr right) {
		setOp(op);
		setLeft(left);
		setRight(right);
	}
	
	public IAstBinExpr copy(IAstNode copyParent) {
		return fixup(this, new AstBinExpr(oper, doCopy(left, copyParent), doCopy(right, copyParent)));
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(oper.getName());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { left, right };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		setLeft((IAstTypedExpr) children[0]);
		setRight((IAstTypedExpr) children[1]);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getLeft() == existing) {
			setLeft((IAstTypedExpr) another);
		} else if (getRight() == existing) {
			setRight((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getOp()
	 */
	@Override
	public IBinaryOperation getOp() {
		return oper;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setOp(org.ejs.eulang.ast.IOperation)
	 */
	@Override
	public void setOp(IBinaryOperation operator) {
		Check.checkArg(operator);
		this.oper = operator;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getLeft()
	 */
	@Override
	public IAstTypedExpr getLeft() {
		return left;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getRight()
	 */
	@Override
	public IAstTypedExpr getRight() {
		return right;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setLeft(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setLeft(IAstTypedExpr expr) {
		left = reparent(left, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setRight(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setRight(IAstTypedExpr expr) {
		right = reparent(right, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		  if (expr instanceof IAstBinExpr
	        && ((IAstBinExpr) expr).getType().equals(getType())
	        && ((IAstBinExpr) expr).getOp() == getOp()) {
			  if (((IAstBinExpr) expr).getLeft().equalValue(getLeft())
					  && ((IAstBinExpr) expr).getRight().equalValue(getRight()))
				  return true;
			  if (oper.isCommutative() && ((IAstBinExpr) expr).getLeft().equalValue(getRight())
					  && ((IAstBinExpr) expr).getRight().equalValue(getLeft()))
				  return true;
		  }
		  return false;
	        
	}
	/*
    public IAstTypedExpr simplify() {
        IAstTypedExpr newLeft = left.simplify();
        IAstTypedExpr newRight = right.simplify();
        
        // it is simplifiable?
        if (operator != K_SUBSCRIPT
                && newLeft instanceof IAstIntegralExpr
                && newRight instanceof IAstIntegralExpr) {
        
            IAstIntegralExpr litLeft = (IAstIntegralExpr) newLeft;
            IAstIntegralExpr litRight = (IAstIntegralExpr) newRight;
            
            switch (operator) {
            case K_ADD:
            case K_SUB:
            case K_MUL:
            case K_DIV: {
                    int intLeft = litLeft.getValue();
                    int intRight = litRight.getValue();
                    int intResult = 0;
                    switch (operator) {
                    case K_ADD:
                        intResult = intLeft + intRight; 
                        break;
                    case K_SUB:
                        intResult = intLeft - intRight; 
                        break;
                    case K_MUL:
                        intResult = intLeft * intRight; 
                        break;
                    case K_DIV:
                        intResult = intLeft / intRight; 
                        break;
                    }
                    IAstIntegralExpr lit = new AstIntegralExpression( 
                            intResult);
                    lit.setParent(getParent());
                    return lit;
                }
            
            case K_AND:
            case K_OR:
            case K_XOR:
            case K_MOD: {
                    int intLeft = litLeft.getValue();
                    int intRight = litRight.getValue();
                    int intResult = 0;
                    switch (operator) {
                    case K_AND:
                        intResult = intLeft & intRight; 
                        break;
                    case K_OR:
                        intResult = intLeft | intRight; 
                        break;
                    case K_XOR:
                        intResult = intLeft ^ intRight; 
                        break;
                    case K_MOD:
                        intResult = intLeft % intRight; 
                        break;
                    }
                    IAstIntegralExpr lit = new AstIntegralExpression(
                            intResult);
                    lit.setParent(getParent());
                    return lit;
            }
                }
        }
        
        // fallthrough: make new binary expression if children changed
        if (!newLeft.equalValue(left) || !newRight.equalValue(right)) {
            newLeft.setParent(null);
            newRight.setParent(null);
            IAstBinaryExpression bin = new AstBinaryExpression(operator, newLeft, newRight);
            bin.setParent(getParent());
            return bin;
        } else {
			return this;
		}
    }

    public boolean equalValue(IAstTypedExpr expr) {
        if (!(expr instanceof IAstBinaryExpression)) {
			return false;
		}
        if (((IAstBinaryExpression) expr).getOperator() != getOperator()) {
			return false;
		}
        if (((IAstBinaryExpression) expr).getLeftOperand().equalValue(getLeftOperand())
        && ((IAstBinaryExpression) expr).getRightOperand().equalValue(getRightOperand())) {
			return true;
		}
        if (isCommutative() 
                && ((IAstBinaryExpression) expr).getLeftOperand().equalValue(getRightOperand())
                && ((IAstBinaryExpression) expr).getRightOperand().equalValue(getLeftOperand())) {
			return true;
		}
        return false;
    }
	*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		if (!(canInferTypeFrom(left) || canInferTypeFrom(right) || canInferTypeFrom(this)))
			return false;
		
		IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		types.left = left.getType();
		types.right = right.getType();
		types.result = getType();
		oper.inferTypes(typeEngine, types);
		
		if (updateType(left, types.left) | updateType(right, types.right) | updateType(this, types.result)) {
			types.left = left.getType();
			types.right = right.getType();
			types.result = getType();
			if (types.left != null && types.right != null && types.result != null) {
				oper.castTypes(typeEngine, types);
				setLeft(createCastOn(typeEngine, left, types.left));
				setRight(createCastOn(typeEngine, right, types.right));
			}
			return true;
		}
		return false;
	}
}
