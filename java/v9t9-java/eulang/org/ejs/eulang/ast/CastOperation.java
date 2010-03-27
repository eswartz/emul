/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.ast.impl.Operation;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class CastOperation extends Operation implements IUnaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public CastOperation(String name) {
		super(name, false);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#getPreferredType(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getPreferredType(TypeEngine typeEngine, LLType lhsType,
			LLType opType) {
		if (lhsType != null)
			return lhsType;
		
		return opType;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#getResultType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getResultType(LLType type) throws TypeException {
		return type;
	}

}
