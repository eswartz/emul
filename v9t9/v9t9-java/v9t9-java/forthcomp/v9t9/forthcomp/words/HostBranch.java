/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class HostBranch extends BaseHostBranch {

	/**
	 * 
	 */
	public HostBranch() {
	}
	

	/**
	 * @param target
	 */
	public HostBranch(int target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return "BRANCH " + target;
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.setHostPc(target);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new HostBranch(target);
	}

}
