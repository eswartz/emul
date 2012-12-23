/**
 * 
 */
package v9t9.machine.ti99.machine;


import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryModel;
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
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new EnhancedTI994AMemoryModel(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public IVdpChip createVdp(IMachine machine) {
		return new VdpV9938(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.machine.StandardMachineModel#isModelCompatible(java.lang.String)
	 */
	@Override
	public boolean isModelCompatible(String machineModel) {
		return machineModel.equals(ID) || super.isModelCompatible(machineModel);
	}
}
