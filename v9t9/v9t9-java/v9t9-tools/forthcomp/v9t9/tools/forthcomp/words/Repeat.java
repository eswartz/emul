/*
  Repeat.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Repeat extends BaseStdWord {
	public Repeat() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		int a = hostContext.popData();
		int b = hostContext.popData();
		
		// HACK
		//int c = 0;
		//if (targetContext instanceof F99TargetContext)
		//	c = hostContext.popData();
		
		new Again().execute(hostContext, targetContext);
		
		//if (targetContext instanceof F99TargetContext)
		//	hostContext.pushData(c);
		
		hostContext.pushData(b);
		hostContext.pushData(a - 2);
		
		new Then().execute(hostContext, targetContext);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
