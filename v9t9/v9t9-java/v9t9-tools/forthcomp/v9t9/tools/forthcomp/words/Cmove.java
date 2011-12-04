/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Cmove extends BaseStdWord {
	public Cmove() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		int len = hostContext.popData();
		int taddr = hostContext.popData();
		if (taddr < 0) taddr = targetContext.resolveAddr(taddr);
		int faddr = hostContext.popData();
		if (faddr < 0) faddr = targetContext.resolveAddr(faddr);
		while (len-- > 0) {
			targetContext.writeChar(taddr++, targetContext.readChar(faddr++));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
