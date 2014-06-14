/*
  DotQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class DotQuote extends BaseWord {

	/**
	 * 
	 */
	public DotQuote() {
		setInterpretationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				new HostType().execute(hostContext, targetContext);
			}
		});
		
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getCompilationSemantics().execute(hostContext, targetContext);
				IWord hostType = hostContext.require("type");
				ITargetWord type = targetContext.require("type");
				hostContext.compileWord(targetContext, hostType, type);
			}
		});
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
