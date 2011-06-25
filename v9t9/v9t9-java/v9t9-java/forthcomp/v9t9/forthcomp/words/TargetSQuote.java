/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TargetSQuote extends TargetWord {
	/**
	 * @param entry
	 */
	public TargetSQuote(DictEntry entry) {
		super(entry);
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				//targetContext.compile((ITargetWord) targetContext.require("((s\"))"));
				targetContext.compileCall((ITargetWord) targetContext.require("((s\"))"));
			}
		});
		
	}

}
