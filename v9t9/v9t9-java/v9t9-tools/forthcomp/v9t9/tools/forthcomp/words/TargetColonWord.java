/*
  TargetColonWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class TargetColonWord extends TargetWord implements ITargetWord {

	/**
	 * @param entry
	 */
	public TargetColonWord(DictEntry entry) {
		super(entry);
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (getHostDp() >= 0 && !getEntry().isTargetOnly()) {
					if (HostContext.DEBUG)
						System.out.println("T> call " + Integer.toHexString(getEntry().getAddr()) + "(" + getHostDp() + ")");
					hostContext.pushCall(getHostDp());
					hostContext.interpret(hostContext, targetContext);
				} else {
					throw hostContext.abort("cannot execute target word: " + getEntry().getName());
				}		
			}
		});
	}

}
