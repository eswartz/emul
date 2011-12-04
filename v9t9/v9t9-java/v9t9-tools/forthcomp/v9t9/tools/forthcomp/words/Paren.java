/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

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

	
	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
