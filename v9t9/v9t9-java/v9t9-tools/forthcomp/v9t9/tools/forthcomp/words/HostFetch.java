/*
  HostFetch.java

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
@HostWordPlaceholder("@")
public class HostFetch extends BaseStdWord {
	@Override
	public String toString() {
		return "@";
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.pushData(targetContext.readCell(hostContext.popData() & 0x7fffffff));
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
