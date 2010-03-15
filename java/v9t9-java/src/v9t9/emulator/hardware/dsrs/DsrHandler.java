/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.engine.memory.MemoryDomain;

/**
 * Java code that handles the work of a DSR through the Idsr instruction.
 * @author ejs
 *
 */
public interface DsrHandler extends IPersistable {

	String GROUP_DSR_SELECTION = "Device Selection";
	String GROUP_DISK_CONFIGURATION = "Disk Configuration";

	void dispose();
	
	/** Get the CRU base */
	short getCruBase();

	/** Handle the DSR call (DSR opcode in mapped ROM)
	 * @param xfer method of moving memory around
	 * @param code the operand of the Idsr instruction
	 * @return true if handled the operand, false if the device doesn't match
	 */
	boolean handleDSR(MemoryTransfer xfer, short code);

	/** Activate the DSR (should be called when the ROM memory entry is mapped) 
	 * @param console
	 * @throws IOException */
	void activate(MemoryDomain console) throws IOException;
	/** Dectivate the DSR (should be called when the ROM memory entry is unmapped) 
	 * @param console */
	void deactivate(MemoryDomain console);

	String getName();
	
	/**
	 * Get editable settings
	 * @return map of group label to settings
	 */
	Map<String, Collection<SettingProperty>> getEditableSettingGroups();
}
