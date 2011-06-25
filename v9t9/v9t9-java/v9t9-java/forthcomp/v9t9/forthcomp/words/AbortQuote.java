/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class AbortQuote extends BaseWord {

	/**
	 * 
	 */
	public AbortQuote() {
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				
				
				int addr = hostContext.popData();
				int leng = hostContext.popData();
				
				if (hostContext.popData() != 0) {
					StringBuilder sb = new StringBuilder();
					while (leng-- > 0)
						sb.append((char) targetContext.readChar(addr++));
					
					throw hostContext.abort(sb.toString());
				}
			}
		});
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getCompilationSemantics().execute(hostContext, targetContext);
				
				targetContext.compile((ITargetWord) targetContext.require("(abort\")"));
			}
		});
	}
}
