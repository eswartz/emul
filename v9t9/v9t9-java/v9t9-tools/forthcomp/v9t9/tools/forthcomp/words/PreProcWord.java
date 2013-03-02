/*
  PreProcWord.java

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

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.IWord;

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
