/*
  Postpone.java

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
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class Postpone extends BaseStdWord {
	public Postpone() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String name = hostContext.readToken();

		IWord word = targetContext.find(name);
		if (word == null) {
			throw hostContext.abort("cannot find target definition of " + name);
		}
		
		if (!(word instanceof ITargetWord))
			throw hostContext.abort("cannot postpone host word " + name);
		
		ITargetWord targetWord = (ITargetWord)word;
		if (targetWord.getEntry().isImmediate()) {
			targetContext.compileCall(targetWord);
		} else {
			targetContext.compilePostpone(targetWord);
		}
		
		IWord hostWord = hostContext.find(name);
		//IWord hostWord = targetWord.getEntry().getHostBehavior();
		//if (hostWord == null)
		//	hostContext.find(name);
		/*
		if ((targetWord.getEntry().isTargetOnly() && hostWord != null)
				|| (!(targetWord instanceof TargetColonWord) && hostWord != null)) {
			//System.out.println(hostContext.getStream().getLocation()+": using host definition when emulating " + name);
			if (hostWord.isImmediate())
				hostContext.compile(hostWord);
			else
				hostContext.compile(new HostPostponedWord(hostWord, targetWord));
		} else if (targetWord.getEntry().getHostBehavior() != null) {
			if (targetWord.getEntry().isImmediate())
				hostContext.compile(targetWord.getEntry().getHostBehavior());
			else
				hostContext.compile(new HostPostponedWord(targetWord.getEntry().getHostBehavior(), targetWord));
		} else if (targetWord instanceof TargetColonWord) {
			if (targetWord.getEntry().isImmediate())
				hostContext.compile(targetWord);
			else
				hostContext.compile(new HostPostponedWord(targetWord, targetWord));
		} else {
			targetContext.markHostExecutionUnsupported();
		}
		*/
		//if (hostWord.getInterpretSemantics() == null && hostWord.getCompileSemantics() == null)
		//      throw hostContext.abort(word.getName() + " cannot be postponed -- no interpret or compile semantics");
		
		hostContext.compile(new HostPostponedWord(hostWord, targetWord));

	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
