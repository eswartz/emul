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
public class Paren extends BaseWord {
	public Paren() {
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				String tok;
				do {
					tok = hostContext.readToken();
					if (tok == null)
						throw hostContext.abort("end of file before )");
				} while (!tok.equals(")"));				
			}
		});
		setCompilationSemantics(getExecutionSemantics());
	}

}
