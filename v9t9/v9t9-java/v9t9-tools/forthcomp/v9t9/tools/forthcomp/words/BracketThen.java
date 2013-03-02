/*
  BracketThen.java

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
 * If and family.
 * 
 * When a word is defined, push 1 and continue parsing.  The else/then/... words will need to skip
 * (by seeing a 1 on the stack).
 * 
 * Else, consume words until else/then/etc is seen and push 0.  Unparse the trigger word.
 * @author ejs
 *
 */
public class BracketThen extends PreProcWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
	}
}
