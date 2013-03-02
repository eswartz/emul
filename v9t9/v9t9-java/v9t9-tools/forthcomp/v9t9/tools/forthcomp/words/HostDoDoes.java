/*
  HostDoDoes.java

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
import v9t9.tools.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class HostDoDoes extends BaseStdWord {

	private final int redirectDp;
	private final int targetDP;

	/**
	 * @param redirectDp
	 * @param targetDP 
	 * @param targetOnly TODO
	 * 
	 */
	public HostDoDoes(int redirectDp, int targetDP, boolean targetOnly) {
		this.redirectDp = redirectDp;
		this.targetDP = targetDP;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.popCall();
		hostContext.compile(new HostBranch(redirectDp));
		
		ITargetWord lastWord = (ITargetWord) targetContext.getLatest();
		targetContext.redefine(lastWord, new TargetDoesWord(lastWord, redirectDp));
		
		lastWord.getEntry().setDoesWord(true);

		targetContext.compileDoes(hostContext, lastWord.getEntry(), targetDP);
		
		//hostContext.setHostPc(redirectDp);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
	
}
