/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

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
