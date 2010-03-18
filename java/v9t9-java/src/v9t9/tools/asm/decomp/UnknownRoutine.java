/**
 * 
 */
package v9t9.tools.asm.decomp;

import v9t9.tools.asm.common.HighLevelInstruction;

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
	public boolean isReturn(HighLevelInstruction inst) {
		return false;
	}

}
