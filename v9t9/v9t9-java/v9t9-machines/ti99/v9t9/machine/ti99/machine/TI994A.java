package v9t9.machine.ti99.machine;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachineModel;



public class TI994A extends TI99Machine {
	public TI994A(ISettingsHandler settings) {
		this(settings, new StandardMachineModel());
	}
	
    public TI994A(ISettingsHandler settings, IMachineModel machineModel) {
        super(settings, machineModel);
    }
}

