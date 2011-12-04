/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.common.cpu.InstructionWorkBlock;

/**
 * This interface receives details about an instruction's effects
 * @author ejs
 *
 */
public interface IInstructionListener {
	void executed(InstructionWorkBlock before, InstructionWorkBlock after);
}
