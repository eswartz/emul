package v9t9.common.dsr;

import java.util.List;


import v9t9.base.settings.ISettingSection;
import v9t9.common.cpu.InstructionWorkBlock;

public interface IDsrManager {

	void dispose();

	void saveState(ISettingSection section);

	void loadState(ISettingSection section);

	/**
	 * @return the dsrs
	 */
	List<IDsrHandler> getDsrs();

	void registerDsr(IDsrHandler dsr);

	/**
	 *	Your DSR module may use the DSR opcode range (OP_DSR) to make
	 *	callbacks into V9t9 to handle subroutine calls.  This routine
	 *	handles those opcodes by calling the DSR module 'filehandler'
	 *	callback.
	 */
	void handleDSR(InstructionWorkBlock instructionWorkBlock);

}