/*
  HostModeStart.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class HostModeStart extends BaseStdWord {
	@Override
	public boolean isImmediate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.BaseStdWord#execute(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.TargetContext)
	 */
	@Override
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.setHostMode(true);
	}

}
