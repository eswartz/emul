package v9t9.emulator.hardware;



public class TI994A extends TI99Machine {
	public TI994A() {
		this(new StandardMachineModel());
	}
	
    public TI994A(MachineModel machineModel) {
        super(machineModel);
    }
}

