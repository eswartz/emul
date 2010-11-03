/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public abstract class HostUnaryOp extends BaseWord {

	private String name;

	public HostUnaryOp(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		int v = hostContext.popData();
		hostContext.pushData(getResult(v));
	}
	
	abstract public int getResult(int v);
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
