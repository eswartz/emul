/*
  BaseTI99MachineModel.java

  (c) 2010-2012 Edward Swartz

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

import java.util.ArrayList;
import java.util.List;

import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.Settings;
import v9t9.engine.speech.SpeechTMS5220;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * @author ejs
 *
 */
public abstract class BaseTI99MachineModel implements IMachineModel {
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		return new Cpu9900(machine, machine.getVdp());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<IDeviceSettings> getDeviceSettings(IMachine machine) {
		List<IDeviceSettings> settings = new ArrayList<IDeviceSettings>();
		IDsrManager dsrManager = machine instanceof TI99Machine ? ((TI99Machine) machine).getDsrManager() : null;
		if (dsrManager != null)  {
			for (IDsrHandler handler : dsrManager.getDsrs()) {
				settings.add(handler);
			}
		}
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine) {
		List<IDeviceIndicatorProvider> list = new ArrayList<IDeviceIndicatorProvider>();
		IDsrManager dsrManager = machine instanceof TI99Machine ? ((TI99Machine) machine).getDsrManager() : null;
		if (dsrManager != null)  {
			for (IDsrHandler handler : dsrManager.getDsrs()) {
				list.addAll(handler.getDeviceIndicatorProviders());
			}
		}
		return list;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineModel#createSpeechChip(v9t9.engine.machine.IMachine)
	 */
	@Override
	public ISpeechChip createSpeechChip(final IMachine machine) {
		IMemoryDomain domain = machine.getMemory().getDomain(IMemoryDomain.NAME_SPEECH);
		if (domain == null)
			return null;
		final SpeechTMS5220 speech = new SpeechTMS5220(machine, Settings.getSettings(machine), domain);
		return speech;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#isModelCompatible(java.lang.String)
	 */
	@Override
	public boolean isModelCompatible(String machineModel) {
		return getIdentifier().equals(machineModel);
	}
}