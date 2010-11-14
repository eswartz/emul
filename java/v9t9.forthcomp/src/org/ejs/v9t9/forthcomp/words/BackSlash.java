/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class BackSlash extends BaseWord {
	public BackSlash() {
		setInterpretationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.getStream().readToEOL();
			}
		});
		setCompilationSemantics(getInterpretationSemantics());
	}
}
