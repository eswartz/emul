/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;

/**
 * @author ejs
 *
 */
public class CodeRelation extends BaseRelation {

	public CodeRelation(ITyped head, ITyped tails) {
		super(head, tails);
	}

	public CodeRelation(ITyped head, ITyped[] tails) {
		super(head, tails);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		if (head.getType() == null)
			return false;
		
		if (!(head.getType() instanceof LLCodeType))
			throw new TypeException("expected code block");
		
		LLCodeType codeType = (LLCodeType) head.getType();

		// children are the return and then argument types
		if (tails.length != codeType.getArgTypes().length + 1) 
			throw new TypeException("argument count does not match");
		
		boolean changed = false;
		
		if (canReplaceType(tails[0], codeType.getRetType())) {
			tails[0].setType(codeType.getRetType());
			changed = true;
		}
		for (int idx = 1; idx < tails.length; idx++) {
			LLType argType = codeType.getArgTypes()[idx - 1];
			if (canReplaceType(tails[idx], argType)) {
				tails[idx].setType(argType);
				changed = true;
			}
		}

		if (changed)
			updateComplete();

		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferUp()
	 */
	@Override
	public boolean inferUp(TypeEngine typeEngine) throws TypeException {
		LLCodeType codeType = null;
		if (head.getType() != null && !(head.getType() instanceof LLCodeType))
			throw new TypeException("expected code block");
		
		codeType = (LLCodeType) head.getType();
		
		LLType infRetType = null;
		LLType[] infArgTypes = new LLType[tails.length - 1];
		
		if (codeType != null) {
			infRetType = codeType.getRetType();
			System.arraycopy(codeType.getArgTypes(), 0, infArgTypes, 0, infArgTypes.length);
		}
		
		boolean changed = false;
		
		if (infRetType == null)
			infRetType = tails[0].getType();
		
		for (int argIdx = 0; argIdx < infArgTypes.length; argIdx++) {
			ITyped arg  = tails[argIdx + 1];
			
			if (arg.getType() != null && (infArgTypes[argIdx] == null 
					|| arg.getType().isMoreComplete(infArgTypes[argIdx]))) { 
				infArgTypes[argIdx] = arg.getType();
			}
		}
		
		codeType = typeEngine.getCodeType(infRetType, infArgTypes);
		
		if (canReplaceType(head, codeType)) {
			head.setType(codeType);
			changed = true;
		}
		
		return changed;
	}

}
