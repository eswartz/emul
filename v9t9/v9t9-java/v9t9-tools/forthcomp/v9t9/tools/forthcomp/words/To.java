/*
  To.java

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
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.LocalVariableTriple;

/**
 * Placeholder for local used in a colon-colon def
 * @author ejs
 *
 */
public class To extends BaseWord {
	/**
	 * 
	 */
	public To() {
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				String name = hostContext.readToken();
				
				LocalVariableTriple triple = ((ITargetWord) targetContext.getLatest()).getEntry().findLocal(name);
				if (triple != null) {
					targetContext.compileToLocal(triple.var.getIndex());
					return;
				}
				
				IWord word = targetContext.find(name);
				if (word == null)
					throw hostContext.abort(name + "?");
				
				if (!(word instanceof TargetValue))
					throw hostContext.abort("cannot handle " + name);
				
				targetContext.markHostExecutionUnsupported();
				targetContext.compileToValue(hostContext, (TargetValue) word);
				if (getEntry() != null)
					getEntry().use();
			}
		});
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				String name = hostContext.readToken();
				
				IWord word = targetContext.find(name);
				if (word == null)
					throw hostContext.abort(name + "?");
				
				if (!(word instanceof TargetValue))
					throw hostContext.abort("cannot handle " + name);
				
				((TargetValue) word).setValue(targetContext, hostContext.popData());
			}
		});
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
