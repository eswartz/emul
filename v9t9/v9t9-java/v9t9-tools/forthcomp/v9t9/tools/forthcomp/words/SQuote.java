/*
  SQuote.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
