/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TargetDoesWord extends TargetWord {

	private final ITargetWord var;
	private final int doesPc;

	/**
	 * @param lastEntry
	 */
	public TargetDoesWord(ITargetWord var_, int doesPc_) {
		super(var_.getEntry());
		this.var = var_;
		this.doesPc = doesPc_;
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.pushData(var.getEntry().getParamAddr());
				hostContext.pushCall(doesPc);
				hostContext.interpret(hostContext, targetContext);				
			}
		});
	}
	
}
