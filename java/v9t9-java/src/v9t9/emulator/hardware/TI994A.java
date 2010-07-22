/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator.hardware;

import v9t9.emulator.common.Machine;

public class TI994A extends TI99Machine {

	public TI994A() {
		this(new StandardMachineModel());
	}
	
    public TI994A(MachineModel machineModel) {
        super(machineModel);
    }
}

