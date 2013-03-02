/*
  SQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import ejs.base.utils.Pair;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class SQuote extends BaseWord {
 
	/**
	 * 
	 */
	public SQuote() {
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				StringBuilder sb = parseString(hostContext);

				Pair<Integer, Integer> addr = targetContext.writeLengthPrefixedString(sb.toString());
				hostContext.pushData(addr.first + 1);
				hostContext.pushData(sb.length());
			}
		});
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				StringBuilder sb = parseString(hostContext);

				targetContext.compileString(hostContext, sb.toString());
			}
		});
	}
	private StringBuilder parseString(HostContext hostContext)
			throws AbortException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			char ch = hostContext.getStream().readChar();
			if (ch == 0)
				break;
			if (ch == '"')
				return sb;
			if (!Character.isWhitespace(ch)) {
				sb.append(ch);
				break;
			}
		}
		
		while (true) {
			char ch = hostContext.getStream().readChar();
			if (ch == 0 || ch == '"')
				break;
			sb.append(ch);
		}
		return sb;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
