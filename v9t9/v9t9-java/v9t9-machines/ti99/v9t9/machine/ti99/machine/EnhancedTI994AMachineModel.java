/*
  EnhancedTI994AMachineModel.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
