/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class HostBehavior extends BaseStdWord {
	public HostBehavior() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String countStr = hostContext.readToken();
		int count;
		try {
			count = Integer.parseInt(countStr);
		} catch (NumberFormatException e) {
			throw hostContext.abort("Expected a number: " + countStr);
		}
		String rparen = hostContext.readToken();
		if (!")".equals(rparen))
			throw hostContext.abort("Expected ): " + rparen);
		
		String word = hostContext.readToken();
		((ITargetWord) targetContext.getLatest()).getEntry().setHostBehavior(
				count, hostContext.require(word));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
