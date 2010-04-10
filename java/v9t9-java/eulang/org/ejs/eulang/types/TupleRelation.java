/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;


/**
 * @author ejs
 *
 */
public class TupleRelation extends BaseRelation  {

	public TupleRelation(ITyped head, ITyped tails) {
		super(head, tails);
	}

	public TupleRelation(ITyped head, ITyped[] tails) {
		super(head, tails);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferUp()
	 */
	@Override
	public boolean inferUp(TypeEngine typeEngine) throws TypeException {
		// TODO Auto-generated method stub
		return false;
	}

}
