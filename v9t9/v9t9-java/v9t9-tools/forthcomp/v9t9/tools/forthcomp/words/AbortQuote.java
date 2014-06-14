/*
  AbortQuote.java

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

/**
 * @author ejs
 *
 */
public class AbortQuote extends BaseWord {

	/**
	 * 
	 */
	public AbortQuote() {
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				
				
				int addr = hostContext.popData();
				int leng = hostContext.popData();
				
				if (hostContext.popData() != 0) {
					StringBuilder sb = new StringBuilder();
					while (leng-- > 0)
						sb.append((char) targetContext.readChar(addr++));
					
					throw hostContext.abort(sb.toString());
				}
			}
		});
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getCompilationSemantics().execute(hostContext, targetContext);
				
				targetContext.compile(targetContext.require("(abort\")"));
			}
		});
	}
}
