/**
 * 
 */
package v9t9.forthcomp.words;

import java.util.Map;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class CompileComma extends BaseStdWord {

	@Override
	public String toString() {
		return "compile,";
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		int val = hostContext.popData();
		for (Map.Entry<String, IWord> entry : targetContext.getDictionary().entrySet()) {
			if (entry.getValue() instanceof ITargetWord
					&& ((ITargetWord) entry.getValue()).getEntry().getContentAddr() == val) {
				targetContext.compile((ITargetWord) entry.getValue());
				return;
			}
		}
		throw hostContext.abort("cannot compile, a host word at " + val);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
