/*
  IMachineModel.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import java.net.URL;
import java.util.List;

import v9t9.common.cassette.ICassetteChip;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.IGplChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardState;
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
	IGplChip createGpl(IMachine machine);

	ISoundChip createSoundChip(IMachine machine);
	
	ISpeechChip createSpeechChip(IMachine machine);

	ICassetteChip createCassetteChip(IMachine machine);

	List<IDeviceSettings> getDeviceSettings(IMachine machine);

	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine);

	IModuleManager createModuleManager(IMachine machine);

	IKeyboardState createKeyboardState(IMachine machine);
	
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
