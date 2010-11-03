/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Host0Branch extends BaseHostBranch {

	/**
	 * 
	 */
	public Host0Branch() {
	}
	
	/**
	 * @param target
	 */
	public Host0Branch(int target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "0BRANCH " + target;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		if (hostContext.popData() == 0)
			hostContext.setHostPc(target);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Host0Branch(target);
	}
}
