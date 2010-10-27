/**
 * 
 */
package org.ejs.v9t9.forthcomp;

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
	public TargetValue(DictEntry entry, int cells) {
		super(entry);
		this.cells = cells;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData(targetContext.readCell(getEntry().getParamAddr()));
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
