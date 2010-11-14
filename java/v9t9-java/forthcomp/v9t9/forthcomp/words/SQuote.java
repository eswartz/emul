/**
 * 
 */
package v9t9.forthcomp.words;

import org.ejs.coffee.core.utils.Pair;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class SQuote extends BaseWord {
 
	/**
	 * 
	 */
	public SQuote() {
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				StringBuilder sb = parseString(hostContext);

				Pair<Integer, Integer> addr = targetContext.writeLengthPrefixedString(sb.toString());
				hostContext.pushData(sb.length());
				hostContext.pushData(addr.first + 1);
			}
		});
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				StringBuilder sb = parseString(hostContext);

				targetContext.compileString(hostContext, sb.toString());
			}
		});
	}
	private StringBuilder parseString(HostContext hostContext)
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
		return sb;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
