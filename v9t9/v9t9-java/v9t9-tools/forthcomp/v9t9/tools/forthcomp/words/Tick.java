/*
  Tick.java

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
import v9t9.tools.forthcomp.RelocEntry.RelocType;

/**
 * @author ejs
 *
 */
public class Tick extends BaseStdWord {
	public Tick() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String name = hostContext.readToken();

		IWord word = targetContext.find(name);
		if (word == null) {
			word = targetContext.defineForward(name, hostContext.getStream().getLocation());
			System.out.println("*** Warning: tick'ing forward word; next word should be ','!");
			targetContext.addRelocation(targetContext.getDP(), RelocType.RELOC_ABS_ADDR_16, 
					((ITargetWord)word).getEntry().getContentAddr());
		}
		
		if (!(word instanceof ITargetWord))
			throw hostContext.abort("cannot take address of host word " + name);
		
		hostContext.pushData(((ITargetWord)word).getEntry().getContentAddr());
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
