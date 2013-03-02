/*
  ColonColon.java

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
 *  :: word ( local1 local2 ... -- ignored ... ) 
 * @author ejs
 *
 */
public class ColonColon extends BaseStdWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		
		
		targetContext.ensureLocalSupport(hostContext);
		
		new Colon().execute(hostContext, targetContext);

		// set up
		
		targetContext.compileSetupLocals(hostContext);
		
		// now parse locals
		
		String token = hostContext.readToken();
		if (!"(".equals(token))
			throw hostContext.abort("Expected (");
		
		ITargetWord theWord = (ITargetWord) targetContext.getLatest();

		theWord.getEntry().allocLocals();
		
		boolean hitParen = false;
		while (true) {
			String name = hostContext.readToken();
			if ("--".equals(name)) 
				break;
			if (")".equals(name)) {
				hitParen = true;
				break;
			}

			theWord.getEntry().defineLocal(name);
			
		}
		targetContext.compileAllocLocals(theWord.getEntry().getLocalCount());
		
		while (!hitParen) {
			String tok = hostContext.readToken();
			if (tok == null)
				throw hostContext.abort("expected )");
			if (")".equals(tok))
				hitParen = true;
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
