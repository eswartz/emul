/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class SQuote extends BaseWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			char ch = hostContext.getStream().readChar();
			if (ch == 0)
				break;
			if (!Character.isWhitespace(ch)) {
				sb.append(ch);
				break;
			}
		}
		
		while (true) {
			char ch = hostContext.getStream().readChar();
			if (ch == 0 || ch == '"')
				break;
			sb.append(ch);
		}

		if (hostContext.isCompiling()) {
			targetContext.compileString(sb.toString());
		} else {
			Pair<Integer, Integer> addr = targetContext.writeLengthPrefixedString(sb.toString());
			hostContext.pushData(sb.length());
			hostContext.pushData(addr.first + 1);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
