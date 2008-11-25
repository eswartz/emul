/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.memory.StandardConsoleMemoryModel;
import v9t9.emulator.hardware.memory.VdpRamArea;
import v9t9.emulator.hardware.memory.mmio.Vdp9918AMmio;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryModel;

/**
 * @author ejs
 *
 */
public class StandardMachineModel implements MachineModel {

	private StandardConsoleMemoryModel memoryModel;

	public StandardMachineModel() {
		memoryModel = new StandardConsoleMemoryModel();
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
		VdpTMS9918A vdp = new VdpTMS9918A(
				memoryModel.VIDEO,
				new Vdp9918AMmio(machine.getMemory(), memoryModel.VIDEO,
						new MemoryEntry("VDP RAM", memoryModel.VIDEO, 0x0000, 0x4000, 
								new VdpRamArea(0x4000))),
				new ImageDataCanvas24Bit());
		return vdp;
	}
	
}
