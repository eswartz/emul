/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.impl.Operation;
import org.ejs.eulang.types.BasicType;
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
		super(name, null, false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#inferTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (types.result == null)
			throw new TypeException("cannot determine cast type");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {

		// see if types are allowed
		if (types.expr.getBasicType() != types.result.getBasicType()) {
			if ((types.expr.getBasicType() == BasicType.POINTER && types.result.getBasicType() == BasicType.ARRAY)
					|| (types.result.getBasicType() == BasicType.ARRAY && types.expr.getBasicType() == BasicType.POINTER)){
				// fine
				return;
			}
			else if (types.expr.getBasicType().isCompatibleWith(types.result.getBasicType())) {
				// fine
				return;
			}
			else if (types.result.getBasicType() == BasicType.VOID || types.expr.getBasicType() == BasicType.VOID) {
				// fine, throwing away or making null
				return;
			}
			else if (types.expr.getBasicType() == BasicType.INTEGRAL && types.result.getBasicType() == BasicType.POINTER) {
				// fine
				return;
			}
			else if (types.expr.getBasicType() == BasicType.CODE && types.result.getBasicType() == BasicType.POINTER) {
				// fine
				return;
			}
			else
				throw new TypeException("cannot cast from " + types.expr +  " to " + types.result);
		}
		
	}

}
