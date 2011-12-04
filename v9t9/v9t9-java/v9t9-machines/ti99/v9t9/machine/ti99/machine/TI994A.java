package v9t9.machine.ti99.machine;

import v9t9.engine.memory.IMachineModel;



public class TI994A extends TI99Machine {
	public TI994A() {
		this(new StandardMachineModel());
	}
	
    public TI994A(IMachineModel machineModel) {
        super(machineModel);
    }
}

