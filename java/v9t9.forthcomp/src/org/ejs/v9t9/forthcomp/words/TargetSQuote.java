/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import static v9t9.engine.cpu.InstF99b.Iexit;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;
import org.ejs.v9t9.forthcomp.ITargetWord;

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
