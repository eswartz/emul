/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TargetColonWord extends TargetWord implements ITargetWord {

	/**
	 * @param entry
	 */
	public TargetColonWord(DictEntry entry) {
		super(entry);
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (getHostDp() >= 0 && !getEntry().isTargetOnly()) {
					hostContext.pushCall(getHostDp());
					hostContext.interpret(hostContext, targetContext);
				} else {
					throw hostContext.abort("cannot execute target word: " + getEntry().getName());
				}		
			}
		});
	}

}
