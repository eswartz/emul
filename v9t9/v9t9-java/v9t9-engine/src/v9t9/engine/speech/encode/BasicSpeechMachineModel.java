/*
  BasicSpeechMachineModel.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import java.net.URL;
import java.util.Collections;
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
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.StockMemoryModel;
import v9t9.engine.speech.SpeechTMS5220;

/**
 * @author ejs
 *
 */
public class BasicSpeechMachineModel implements IMachineModel {

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "BasicSpeechMachine";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getName()
	 */
	@Override
	public String getName() {
		return "Speech Machine";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMachine(v9t9.common.client.ISettingsHandler)
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		return new BasicSpeechMachine(settings, this);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMemoryModel(v9t9.common.machine.IMachine)
	 */
	@Override
	public IMemoryModel createMemoryModel(IMachine machine) {
		IMemoryModel model = new StockMemoryModel();
		model.getMemory().addDomain(IMemoryDomain.NAME_SPEECH, new MemoryDomain(IMemoryDomain.NAME_SPEECH));
		return model;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#defineDevices(v9t9.common.machine.IMachine)
	 */
	@Override
	public void defineDevices(IMachine machine) {

	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createCPU(v9t9.common.machine.IMachine)
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createVdp(v9t9.common.machine.IMachine)
	 */
	@Override
	public IVdpChip createVdp(IMachine machine) {
		return null;
	}
	
	@Override
	public IGplChip createGpl(IMachine machine) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createSoundChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ISoundChip createSoundChip(IMachine machine) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createSpeechChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ISpeechChip createSpeechChip(IMachine machine) {
		return new SpeechTMS5220(machine, machine.getSettings(), machine.getMemory().getDomain(
				IMemoryDomain.NAME_SPEECH));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createCassetteChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ICassetteChip createCassetteChip(IMachine machine) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDeviceSettings(v9t9.common.machine.IMachine)
	 */
	@Override
	public List<IDeviceSettings> getDeviceSettings(IMachine machine) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDeviceIndicatorProviders(v9t9.common.machine.IMachine)
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(
			IMachine machine) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createModuleManager(v9t9.common.machine.IMachine)
	 */
	@Override
	public IModuleManager createModuleManager(IMachine machine) {
		return null;
	}
	
	@Override
	public IKeyboardState createKeyboardState(IMachine machine) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDataURL()
	 */
	@Override
	public URL getDataURL() {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#isModelCompatible(java.lang.String)
	 */
	@Override
	public boolean isModelCompatible(String machineModel) {
		return getIdentifier().equals(machineModel);
	}
}
