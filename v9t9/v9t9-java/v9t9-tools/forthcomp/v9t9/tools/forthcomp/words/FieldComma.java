/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.F99bTargetContext;
import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class FieldComma extends BaseStdWord {
	public FieldComma() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {

		int val = hostContext.popData();

		((F99bTargetContext) targetContext).compileByte(val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
