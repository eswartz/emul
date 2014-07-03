/*
  ColonColon.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.TargetContext;

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
