/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class HostLiteral extends BaseWord {

	private final int val;
	private boolean isUnsigned; 
	/**
	 * @param isUnsigned 
	 * 
	 */
	public HostLiteral(int val_, boolean isUnsigned_) {
		this.val = val_;
		this.isUnsigned = isUnsigned_;
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.compile(HostLiteral.this);
				targetContext.compileLiteral(val, isUnsigned, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(val);
			}
		});
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LITERAL "  + val;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return val;
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}
	public boolean isUnsigned() {
		return isUnsigned;
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
