/*
  HostPostponedWord.java

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

import java.util.Stack;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class HostPostponedWord extends BaseWord {

	private final ITargetWord targetWord;
	private final IWord hostWord;

	/**
	 * @param i
	 */
	public HostPostponedWord(IWord hostWord_, ITargetWord targetWord_) {
		this.hostWord = hostWord_;
		this.targetWord = targetWord_;
		setName(targetWord.getEntry().getName());
		
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				//hostContext.compileWord(targetContext, hostWord, targetWord);
				if (!targetWord.getEntry().isTargetOnly()) {
					IWord hostBehavior = targetWord.getEntry().getHostBehavior();
					if (hostBehavior != null) {
						int dp = 0;
						Stack<Integer> origDataStack = null;
						Stack<Integer> origReturnStack = null;
						origDataStack = new Stack<Integer>(); 
						origDataStack.addAll(hostContext.getDataStack());
						origReturnStack = new Stack<Integer>();
						origReturnStack.addAll(hostContext.getReturnStack());
						dp = targetContext.getDP();
						
						targetWord.getExecutionSemantics().execute(hostContext, targetContext);
						
						targetContext.setDP(dp);
						hostContext.getDataStack().clear();
						hostContext.getDataStack().addAll(origDataStack);
						hostContext.getReturnStack().clear();
						hostContext.getReturnStack().addAll(origReturnStack);
						
						if (hostContext.isCompiling())
							hostBehavior.getCompilationSemantics().execute(hostContext, targetContext);
						else
							hostBehavior.getExecutionSemantics().execute(hostContext, targetContext);
					} else {
						if (hostContext.isCompiling() && targetWord.getExecutionSemantics() == null)
							targetWord.getCompilationSemantics().execute(hostContext, targetContext);
						else
							targetWord.getExecutionSemantics().execute(hostContext, targetContext);
					}
				} else if (hostWord != null)
					if (hostContext.isCompiling())
						hostWord.getCompilationSemantics().execute(hostContext, targetContext);
					else
						hostWord.getExecutionSemantics().execute(hostContext, targetContext);
				else
					throw hostContext.abort("cannot invoke POSTPONE'd word -- no host-only definition: " + targetWord.getName());
			}
		});
	}
	

}
