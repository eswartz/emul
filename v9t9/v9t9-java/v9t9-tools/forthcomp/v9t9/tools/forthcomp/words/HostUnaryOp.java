/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public abstract class HostUnaryOp extends BaseStdWord {

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
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		int v = hostContext.popData();
		hostContext.pushData(getResult(v));
	}
	
	abstract public int getResult(int v);
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
