package v9t9.emulator.hardware;

import org.ejs.coffee.core.properties.SettingProperty;


public class TI994A extends TI99Machine {
	public TI994A() {
		this(new StandardMachineModel());
	}
	
    public TI994A(MachineModel machineModel) {
        super(machineModel);
    }
}

