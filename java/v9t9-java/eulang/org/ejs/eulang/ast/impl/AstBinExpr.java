/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ops.LLConstOp;
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
	
	public IAstBinExpr copy() {
		return fixup(this, new AstBinExpr(oper, doCopy(left), doCopy(right)));
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
				  && expr.getType() != null
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
	@Override
    public IAstTypedExpr simplify(TypeEngine typeEngine) {
        IAstTypedExpr newLeft = left.simplify(typeEngine);
        IAstTypedExpr newRight = right.simplify(typeEngine);
        
        // it is simplifiable?
        if (newLeft instanceof IAstLitExpr
                && newRight instanceof IAstLitExpr) {
        
            IAstLitExpr litLeft = (IAstLitExpr) newLeft;
            IAstLitExpr litRight = (IAstLitExpr) newRight;
            
            LLConstOp op = oper.evaluate(getType(), litLeft, litRight);
            if (op != null) {
            	IAstLitExpr lit = typeEngine.createLiteralNode(
            			op.getType(), op.getValue());
            	lit.setSourceRef(getSourceRef());
            	return lit;
            }
        }
        
        // fallthrough: make new binary expression if children changed
        if (!newLeft.equalValue(left) || !newRight.equalValue(right)) {
        	if (newLeft == left)
        		newLeft = (IAstTypedExpr) newLeft.copy();
        	if (newRight == right)
        		newRight = (IAstTypedExpr) newRight.copy();
            IAstBinExpr bin = new AstBinExpr(oper, newLeft, newRight);
            bin.setType(getType());
            bin.setSourceRef(getSourceRef());
            return bin;
        } else {
			return this;
		}
    }

	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		if (!(canInferTypeFrom(left) || canInferTypeFrom(right) || canInferTypeFrom(this)))
			return false;
		
		IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		types.left = left.getType();
		types.leftIsSymbol = left instanceof IAstSymbolExpr;
		types.right = right.getType();
		types.rightIsSymbol = right instanceof IAstSymbolExpr;
		types.result = getType();
		oper.inferTypes(typeEngine, types);
		
		boolean changed = (updateType(left, types.left) | updateType(right, types.right) | updateType(this, types.result));
		
		types.left = left.getType();
		types.right = right.getType();
		types.result = getType();
		if (types.left != null && types.right != null && types.result != null) {
			oper.castTypes(typeEngine, types);
			setLeft(createCastOn(typeEngine, left, types.left));
			setRight(createCastOn(typeEngine, right, types.right));
		}
		return changed;
	}
	
	 /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
     */
    @Override
    public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
    	IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		types.left = left.getType();
		types.right = right.getType();
		types.result = getType();
		oper.validateTypes(typeEngine, types);
		
    }
}
