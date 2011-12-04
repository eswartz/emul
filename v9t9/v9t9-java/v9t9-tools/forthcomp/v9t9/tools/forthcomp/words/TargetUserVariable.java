/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

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
	public TargetUserVariable(String name, int index) {
		super(new DictEntry(0, 0, name));
		this.index = index;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				targetContext.compileUser(TargetUserVariable.this);				
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.pushData(0xff00 + (TargetUserVariable.this.getIndex()) * targetContext.getCellSize());				
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
