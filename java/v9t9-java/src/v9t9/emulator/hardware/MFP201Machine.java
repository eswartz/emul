package v9t9.emulator.hardware;

import v9t9.emulator.common.Machine;

public class MFP201Machine extends Machine {

	public MFP201Machine() {
		super(new MFP201MachineModel());
	}

	@Override
	protected void init(MachineModel machineModel) {
		super.init(machineModel);
		dsrManager = null;
	}
	
}