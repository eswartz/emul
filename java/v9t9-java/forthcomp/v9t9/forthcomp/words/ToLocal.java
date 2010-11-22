/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;

/**
 *  :> name -- new local
 * @author ejs
 *
 */
public class ToLocal extends BaseStdWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		
		hostContext.assertCompiling();

		ITargetWord theWord = (ITargetWord) targetContext.getLatest();

		if (!theWord.getEntry().hasLocals()) {
			if (hostContext.inCSP()) {
				throw hostContext.abort("Cannot first add locals inside conditionals");
			}
			
			if (!targetContext.isLocalSupportAvailable(hostContext)) {

				ITargetWord word = (ITargetWord) targetContext.require("branch");
				word.getCompilationSemantics().execute(hostContext, targetContext);
				hostContext.compile(hostContext.require("branch"));
				
				targetContext.pushFixup(hostContext);
				
				targetContext.ensureLocalSupport(hostContext);	// in the middle of a word!
				
				targetContext.resolveFixup(hostContext);
			}
			targetContext.compileSetupLocals(hostContext);
			theWord.getEntry().allocLocals();
			
			targetContext.setLatest(theWord);
		}

		String name = hostContext.readToken();
		
		theWord.getEntry().defineLocal(name);
			
		targetContext.compileAllocLocals(1);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
