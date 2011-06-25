/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;

/**
 *  :: word ( local1 local2 ... -- ignored ... ) 
 * @author ejs
 *
 */
public class ColonColon extends BaseStdWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		
		
		targetContext.ensureLocalSupport(hostContext);
		
		new Colon().execute(hostContext, targetContext);

		// set up
		
		targetContext.compileSetupLocals(hostContext);
		
		// now parse locals
		
		String token = hostContext.readToken();
		if (!"(".equals(token))
			throw hostContext.abort("Expected (");
		
		ITargetWord theWord = (ITargetWord) targetContext.getLatest();

		theWord.getEntry().allocLocals();
		
		boolean hitParen = false;
		while (true) {
			String name = hostContext.readToken();
			if ("--".equals(name)) 
				break;
			if (")".equals(name)) {
				hitParen = true;
				break;
			}

			theWord.getEntry().defineLocal(name);
			
		}
		targetContext.compileAllocLocals(theWord.getEntry().getLocalCount());
		
		while (!hitParen) {
			String tok = hostContext.readToken();
			if (tok == null)
				throw hostContext.abort("expected )");
			if (")".equals(tok))
				hitParen = true;
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
