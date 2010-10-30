/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.F99TargetContext;
import org.ejs.v9t9.forthcomp.F99bTargetContext;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;
import org.ejs.v9t9.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class FieldComma implements IWord {
	public FieldComma() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {

		int val = hostContext.popData();

		if (targetContext instanceof F99TargetContext)
			((F99TargetContext) targetContext).compileField(val);
		else
			((F99bTargetContext) targetContext).compileByte(val);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
