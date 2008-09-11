/**
 * 
 */
package v9t9.tools.decomp;

/**
 * @author ejs
 *
 */
public class UnknownRoutine extends Routine {

	/* (non-Javadoc)
	 * @see v9t9.tools.decomp.Routine#examineEntryCode()
	 */
	@Override
	public void examineEntryCode() {

	}

	/* (non-Javadoc)
	 * @see v9t9.tools.decomp.Routine#isReturn(v9t9.tools.decomp.LLInstruction)
	 */
	@Override
	public boolean isReturn(LLInstruction inst) {
		return false;
	}

}
