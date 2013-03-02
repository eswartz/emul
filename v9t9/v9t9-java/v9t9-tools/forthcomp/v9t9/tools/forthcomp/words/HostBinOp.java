/*
  HostBinOp.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public abstract class HostBinOp extends BaseStdWord {

	private String name;

	public HostBinOp(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		int r = hostContext.popData();
		int l = hostContext.popData();
		hostContext.pushData(getResult(l, r));
	}
	
	abstract public int getResult(int l, int r);
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
