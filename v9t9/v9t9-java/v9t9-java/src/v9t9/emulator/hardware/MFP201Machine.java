package v9t9.emulator.hardware;

import v9t9.emulator.common.Machine;

public class MFP201Machine extends Machine {

	public MFP201Machine(MFP201MachineModel model) {
		super(model);
	}

	@Override
	protected void init(MachineModel machineModel) {
		super.init(machineModel);
		dsrManager = null;
	}
	
}