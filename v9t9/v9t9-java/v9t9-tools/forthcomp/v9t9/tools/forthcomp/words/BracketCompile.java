/*
  BracketCompile.java

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
public class BracketCompile extends BaseWord {

	/**
	 * 
	 */
	public BracketCompile() {
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				String name = hostContext.readToken();
				IWord word = targetContext.find(name);
				if (word == null) {
					IWord num = targetContext.parseLiteral(name);
					if (num != null) {
						num.getCompilationSemantics().execute(hostContext, targetContext);
						return;
					} else {
						word = targetContext.defineForward(name, hostContext.getStream().getLocation());
					}
				}
				
				if (!(word instanceof ITargetWord))
					throw hostContext.abort("cannot take address of host word " + name);
				
				targetContext.compileTick((ITargetWord) word);
				targetContext.compile((ITargetWord) targetContext.require("compile,"));
			}
		});
	}
}
