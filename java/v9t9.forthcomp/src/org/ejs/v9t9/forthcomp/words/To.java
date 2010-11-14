/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;
import org.ejs.v9t9.forthcomp.ITargetWord;
import org.ejs.v9t9.forthcomp.IWord;
import org.ejs.v9t9.forthcomp.LocalVariableTriple;

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
}
