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
public class EquivalenceRelation extends BaseRelation {

	public EquivalenceRelation(ITyped head, ITyped tails) {
		super(head, tails);
	}

	public EquivalenceRelation(ITyped head, ITyped[] tails) {
		super(head, tails);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		for (ITyped tail : tails) {
			if (tail == null)
				continue;

			if (canReplaceType(tail, head.getType())) {
				tail.setType(head.getType());
				changed = true;
			}
			else if (head.getType().isComplete()) {
				if (!typeEngine.getBaseType(tail.getType()).equals(typeEngine.getBaseType(head.getType()))) {
					throw new TypeException(tail, "types do not match");
				}				
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
		boolean changed = false;
		LLType theType = null;
		for (ITyped tail : tails) {
			if (tail != null) {
				if (theType == null) {
					theType = tail.getType();
				} else {
					if (!typeEngine.getBaseType(theType).equals(typeEngine.getBaseType(tail.getType()))) {
						throw new TypeException(tail, "types do not match");
					}
				}
			}
		}
		
		if (canReplaceType(head, theType)) {
			head.setType(theType);
			changed = true;
		}
		
		return changed;
	}

}
