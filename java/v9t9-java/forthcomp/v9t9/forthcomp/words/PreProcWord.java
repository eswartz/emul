/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public abstract class PreProcWord extends BaseStdWord {

	protected void falseBranch(HostContext hostContext, TargetContext targetContext) throws AbortException {
		int levels = 1;
		while (true) {
			String word = hostContext.readToken();
			//System.out.println("SKIP: " + word);
			if ("(".equals(word) || "\\".equals(word)) {
				IWord realword = hostContext.find(word);
				realword.getInterpretationSemantics().execute(hostContext, targetContext);
				continue;
			}
			
			if ("[if]".equalsIgnoreCase(word) || "[ifndef]".equalsIgnoreCase(word) || "[ifdef]".equalsIgnoreCase(word)) {
				levels++;
			}
			else if ("[else]".equalsIgnoreCase(word)) {
				if (levels - 1 == 0)
					levels--;
			} else if ("[endif]".equalsIgnoreCase(word) || "[then]".equalsIgnoreCase(word)) {
				levels--;
			}
			
			if (levels == 0)
				break;
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
