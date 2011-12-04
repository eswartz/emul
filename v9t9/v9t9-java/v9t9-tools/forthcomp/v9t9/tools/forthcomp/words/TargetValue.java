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
				if (getEntry().canInline())
					targetContext.compileLiteral(getEntry().getParamAddr(), false, true);
				else
					targetContext.compile(TargetValue.this);
					
				//targetContext.compileLoad(getCells() * targetContext.getCellSize());
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
