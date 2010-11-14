/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public abstract class HostBinOp extends BaseStdWord {

	private String name;

	public HostBinOp(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		int r = hostContext.popData();
		int l = hostContext.popData();
		hostContext.pushData(getResult(l, r));
	}
	
	abstract public int getResult(int l, int r);
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
