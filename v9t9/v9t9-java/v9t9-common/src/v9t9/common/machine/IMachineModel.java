/*
  IMachineModel.java

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
package v9t9.common.machine;

import java.net.URL;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.common.machine.IMachine;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface IMachineModel {
	String getIdentifier();

	/** User-visible name, with "the" */
	String getName();

	IMachine createMachine(ISettingsHandler settings);

	IMemoryModel createMemoryModel(IMachine machine);
	
	void defineDevices(IMachine machine);
	
	ICpu createCPU(IMachine machine);
	
	IVdpChip createVdp(IMachine machine);

	ISoundChip createSoundChip(IMachine machine);
	
	ISpeechChip createSpeechChip(IMachine machine);

	List<IDeviceSettings> getDeviceSettings(IMachine machine);

	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine);

	IModuleManager createModuleManager(IMachine machine);

	
	/**
	 * Get the base data URL for this machine
	 * @return
	 */
	URL getDataURL();

	/**
	 * @param machineModel
	 * @return
	 */
	boolean isModelCompatible(String machineModel);
}
