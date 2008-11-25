/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.Machine;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface MachineModel {

	MemoryModel getMemoryModel();
	
	VdpHandler createVdp(Machine machine);
}
