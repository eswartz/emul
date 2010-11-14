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
public class Literal extends BaseWord {

	private final boolean optimize;

	/**
	 * 
	 */
	public Literal(boolean optimize_) {
		this.optimize = optimize_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int val = hostContext.popData();
				targetContext.compileLiteral(val, false, optimize);
				
				hostContext.compile(new HostLiteral(val, false));				
			}
		});
		setInterpretationSemantics(getCompilationSemantics());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LITERAL";
	}
}
