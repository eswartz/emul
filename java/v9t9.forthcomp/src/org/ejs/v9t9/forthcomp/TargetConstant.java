/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class TargetConstant extends TargetWord implements ITargetWord {

	private final int value;
	private final int width;

	/**
	 * @param entry
	 */
	public TargetConstant(DictEntry entry, int value, int width) {
		super(entry);
		this.value = value;
		this.width = width;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.pushData(value & 0xffff);
		if (width == 2)
			hostContext.pushData(value >> 16);
	}
}
