/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;

/**
 * Placeholder for local used in a colon-colon def
 * @author ejs
 *
 */
public class LocalVariable extends BaseStdWord {

	private int index;
	/**
	 * 
	 */
	public LocalVariable(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		targetContext.compileFromLocal(index);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
