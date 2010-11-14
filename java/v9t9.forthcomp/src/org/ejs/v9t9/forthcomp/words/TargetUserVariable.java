/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class TargetUserVariable extends TargetWord {

	private int index;
	/**
	 * @param index 
	 * 
	 */
	public TargetUserVariable(DictEntry entry, int index) {
		super(entry);
		this.index = index;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				targetContext.compileUser(TargetUserVariable.this);				
			}
		});
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
}
