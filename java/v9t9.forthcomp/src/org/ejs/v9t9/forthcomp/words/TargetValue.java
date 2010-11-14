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
public class TargetValue extends TargetWord {
	private int cells;

	/**
	 * @param addr 
	 * 
	 */
	public TargetValue(DictEntry entry, int cells_) {
		super(entry);
		this.cells = cells_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				targetContext.compileLiteral(getEntry().getParamAddr(), false, true);
				targetContext.compileLoad(getCells() * targetContext.getCellSize());
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				int addr = getEntry().getParamAddr();
				if (getCells() == 1)
					hostContext.pushData(targetContext.readCell(addr));
				else if (getCells() == 2) {
					hostContext.pushData(targetContext.readCell(addr + targetContext.getCellSize()));
					hostContext.pushData(targetContext.readCell(addr));
				} else
					assert false;
			}
		});
	}

	/**
	 * @param value
	 */
	public void setValue(TargetContext targetContext, int value) {
		targetContext.writeCell(getEntry().getParamAddr(), value);
	}

	/**
	 * @return
	 */
	public int getCells() {
		return cells;
	}
	
}
