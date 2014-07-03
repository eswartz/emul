/*
  PreProcWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public abstract class PreProcWord extends BaseStdWord {
	/**
	 * 
	 */
	public PreProcWord() {
		super();
	}

	protected void falseBranch(HostContext hostContext, TargetContext targetContext) throws AbortException {
		int levels = 1;
		while (true) {
			String word = hostContext.readToken();
			//System.out.println("SKIP: " + word);
			if ("(".equals(word) || "\\".equals(word)) {
				IWord realword = hostContext.find(word);
				realword.getInterpretationSemantics().execute(hostContext, targetContext);
				continue;
			}
			
			if ("[if]".equalsIgnoreCase(word) || "[ifndef]".equalsIgnoreCase(word) || "[ifdef]".equalsIgnoreCase(word)) {
				levels++;
			}
			else if ("[else]".equalsIgnoreCase(word)) {
				if (levels - 1 == 0)
					levels--;
			} else if ("[endif]".equalsIgnoreCase(word) || "[then]".equalsIgnoreCase(word)) {
				levels--;
			}
			
			if (levels == 0)
				break;
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
