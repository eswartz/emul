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
public class DLiteral extends BaseWord {

	private final boolean optimize;

	/**
	 * 
	 */
	public DLiteral(boolean optimize_) {
		this.optimize = optimize_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int valH = hostContext.popData();
				int valL = hostContext.popData();
				targetContext.compileDoubleLiteral(valL, valH, false, optimize);
				
				hostContext.compile(new HostDoubleLiteral(valL, valH, false));
			}
		});
		setInterpretationSemantics(getCompilationSemantics());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DLITERAL";
	}
	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
