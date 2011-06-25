package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class LastXt extends BaseStdWord {
	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		hostContext.pushData(((ITargetWord) targetContext.getLatest()).getEntry().getContentAddr());
	}

	public boolean isImmediate() {
		return false;
	}
}