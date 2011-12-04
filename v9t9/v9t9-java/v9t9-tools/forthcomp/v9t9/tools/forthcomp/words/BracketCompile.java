/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class BracketCompile extends BaseWord {

	/**
	 * 
	 */
	public BracketCompile() {
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				String name = hostContext.readToken();
				IWord word = targetContext.find(name);
				if (word == null) {
					word = targetContext.defineForward(name, hostContext.getStream().getLocation());
				}
				
				if (!(word instanceof ITargetWord))
					throw hostContext.abort("cannot take address of host word " + name);
				
				targetContext.compileTick((ITargetWord) word);
				targetContext.compile((ITargetWord) targetContext.require("compile,"));
			}
		});
	}
}
