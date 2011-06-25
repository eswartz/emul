package v9t9.emulator.hardware.dsrs;

import java.util.List;

import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.engine.cpu.InstructionWorkBlock;

public interface IDsrManager {

	void dispose();

	void saveState(ISettingSection section);

	void loadState(ISettingSection section);

	/**
	 * @return the dsrs
	 */
	List<DsrHandler> getDsrs();

	void registerDsr(DsrHandler dsr);

	/**
	 *	Your DSR module may use the DSR opcode range (OP_DSR) to make
	 *	callbacks into V9t9 to handle subroutine calls.  This routine
	 *	handles those opcodes by calling the DSR module 'filehandler'
	 *	callback.
	 */
	void handleDSR(InstructionWorkBlock instructionWorkBlock);

}