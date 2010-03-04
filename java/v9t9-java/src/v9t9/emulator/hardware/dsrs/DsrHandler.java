/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.Setting;

import v9t9.engine.memory.MemoryDomain;

/**
 * Java code that handles the work of a DSR through the Idsr instruction.
 * @author ejs
 *
 */
public interface DsrHandler {

	/** Get the CRU base */
	short getCruBase();

	/** Handle the DSR call (DSR opcode in mapped ROM)
	 * @param xfer method of moving memory around
	 * @param code the operand of the Idsr instruction
	 * @return true if handled the operand, false if the device doesn't match
	 */
	boolean handleDSR(MemoryTransfer xfer, short code);

	/** Activate the DSR (should be called when the ROM memory entry is mapped) 
	 * @param console TODO
	 * @throws IOException */
	void activate(MemoryDomain console) throws IOException;
	/** Dectivate the DSR (should be called when the ROM memory entry is unmapped) 
	 * @param console TODO*/
	void deactivate(MemoryDomain console);

	String getName();
	
	void saveState(IDialogSettings section);
	void loadState(IDialogSettings section);

	/**
	 * @return
	 */
	Setting[] getSettings();
}
