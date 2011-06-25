/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Lbracket extends BaseStdWord {
	public Lbracket() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		hostContext.setCompiling(false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
