/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class TargetVariable extends TargetWord {

	/**
	 * @param addr 
	 * 
	 */
	public TargetVariable(DictEntry entry) {
		super(entry);
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				targetContext.compileLiteral(getEntry().getParamAddr(), false, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(getEntry().getParamAddr());
			}
		});
	}
	
}
