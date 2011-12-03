/**
 * 
 */
package v9t9.engine.dsr;

import java.io.IOException;
import java.util.List;


import v9t9.base.properties.IPersistable;
import v9t9.common.memory.MemoryDomain;

/**
 * Java code that handles the work of a DSR through the Idsr instruction.
 * @author ejs
 *
 */
public interface IDsrHandler extends IPersistable, IDsrSettings {

	String GROUP_DSR_SELECTION = "Device Selection";
	String GROUP_DISK_CONFIGURATION = "Disk Configuration";

	void dispose();
	
	/** Handle the DSR call (DSR opcode in mapped ROM)
	 * @param xfer method of moving memory around
	 * @param code the operand of the Idsr instruction
	 * @return true if handled the operand, false if the device doesn't match
	 */
	boolean handleDSR(IMemoryTransfer xfer, short code);

	/** Activate the DSR (should be called when the ROM memory entry is mapped) 
	 * @param console
	 * @throws IOException */
	void activate(MemoryDomain console) throws IOException;
	/** Dectivate the DSR (should be called when the ROM memory entry is unmapped) 
	 * @param console */
	void deactivate(MemoryDomain console);

	String getName();
	
	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders();
}
