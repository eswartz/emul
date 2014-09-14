/*
  BasicSpeechMachineModel.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.ICassetteChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockMemoryModel;

/**
 * @author ejs
 *
 */
public abstract class BasicMachineModelTest implements IMachineModel {

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMachine(v9t9.common.client.ISettingsHandler)
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		return new BasicMachineTest(settings, this);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMemoryModel(v9t9.common.machine.IMachine)
	 */
	@Override
	public IMemoryModel createMemoryModel(IMachine machine) {
		IMemoryModel model = new StockMemoryModel() {
			/* (non-Javadoc)
			 * @see v9t9.engine.memory.StockMemoryModel#initMemory(v9t9.common.machine.IBaseMachine)
			 */
			@Override
			public void initMemory(IBaseMachine machine) {
				super.initMemory(machine);
				ByteMemoryArea area = new ByteMemoryArea(0);
				area.read = area.write = new byte[65536];
				IMemoryEntry entry = new MemoryEntry("RAM", machine.getConsole(), 0, 65536, area);
				machine.getConsole().mapEntry(entry);
			}
		};
		return model;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#defineDevices(v9t9.common.machine.IMachine)
	 */
	@Override
	public void defineDevices(IMachine machine) {

	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createVdp(v9t9.common.machine.IMachine)
	 */
	@Override
	public IVdpChip createVdp(IMachine machine) {
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
		return null;
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
