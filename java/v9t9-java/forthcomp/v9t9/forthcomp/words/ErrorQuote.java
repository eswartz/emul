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
public class ErrorQuote extends BaseWord {

	/**
	 * 
	 */
	public ErrorQuote() {
		setInterpretationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				
				StringBuilder sb = new StringBuilder();
				int addr = hostContext.popData();
				int leng = hostContext.popData();
				while (leng-- > 0)
					sb.append((char) targetContext.readChar(addr++));
				
				throw hostContext.abort(sb.toString());
			}
		});
		
	}
}
