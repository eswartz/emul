/**
 * 
 */
package v9t9.machine.f99b.asm;

import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.Routine;

/**
 * @author ejs
 *
 */
public class F99bRoutine extends Routine {

	@Override
	public boolean isReturn(IHighLevelInstruction inst) {
		return inst.isReturn();
	}

	@Override
	public void examineEntryCode() {

	}

}
