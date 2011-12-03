/**
 * 
 */
package v9t9.machine.ti99.machine;


import v9t9.common.memory.MemoryModel;
import v9t9.engine.hardware.VdpChip;
import v9t9.engine.machine.IMachine;
import v9t9.engine.memory.Vdp9938Mmio;
import v9t9.engine.video.v9938.VdpV9938;
import v9t9.machine.ti99.memory.EnhancedTI994AMemoryModel;

/**
 * @author ejs
 *
 */
public class EnhancedTI994AMachineModel extends StandardMachineModel {

	public static final String ID = "EnhancedTI994A";
	
	public EnhancedTI994AMachineModel() {
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public MemoryModel getMemoryModel() {
		if (memoryModel == null) {
			memoryModel = new EnhancedTI994AMemoryModel();
		}
		return memoryModel;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public VdpChip createVdp(IMachine machine) {
		VdpV9938 vdp = new VdpV9938(machine);
		new Vdp9938Mmio(machine.getMemory(), vdp, 0x10000);
		return vdp;
	}
}
