/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class TargetVariable extends TargetWord {

	private int addr;
	/**
	 * @param addr 
	 * 
	 */
	public TargetVariable(DictEntry entry, int addr) {
		super(entry);
		this.addr = addr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData(addr);
	}
	
}
