/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.emulator.hardware.memory.EnhancedConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;

/**
 * @author ejs
 *
 */
public class EnhancedMachineModel implements MachineModel {

	private EnhancedConsoleMemoryModel memoryModel;

	public EnhancedMachineModel() {
		memoryModel = new EnhancedConsoleMemoryModel();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public MemoryModel getMemoryModel() {
		return memoryModel;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public VdpHandler createVdp(Machine machine) {
		VdpV9938 vdp = new VdpV9938(
				memoryModel.VIDEO,
				new Vdp9938Mmio(machine.getMemory(), memoryModel.VIDEO, 0x20000),
				new ImageDataCanvas24Bit());
		return vdp;
	}

}
