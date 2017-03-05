/*
  HostBehavior.java

  (c) 2010-2014 Edward Swartz

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
 * @author ejs
 *
 */
public class HostBehavior extends BaseStdWord {
	public HostBehavior() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String countStr = hostContext.readToken();
		int count;
		try {
			count = Integer.parseInt(countStr);
		} catch (NumberFormatException e) {
			throw hostContext.abort("Expected a number: " + countStr);
		}
		String rparen = hostContext.readToken();
		if (!")".equals(rparen))
			throw hostContext.abort("Expected ): " + rparen);
		
		String word = hostContext.readToken();
		((ITargetWord) targetContext.getLatest()).getEntry().setHostBehavior(
				count, hostContext.require(word));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
