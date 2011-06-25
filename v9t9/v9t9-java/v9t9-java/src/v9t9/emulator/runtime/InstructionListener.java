/**
 * 
 */
package v9t9.emulator.runtime;

import v9t9.engine.cpu.BaseInstructionWorkBlock;

/**
 * This interface receives details about an instruction's effects
 * @author ejs
 *
 */
public interface InstructionListener {
	void executed(BaseInstructionWorkBlock before, BaseInstructionWorkBlock after);
}
