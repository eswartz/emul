/*
  F99bMachineModel.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import v9t9.common.cassette.ICassetteChip;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.common.settings.Settings;
import v9t9.engine.dsr.realdisk.MemoryDiskImageDsr;
import v9t9.engine.sound.MultiSoundTMS9919B;
import v9t9.engine.speech.SpeechTMS5220;
import v9t9.engine.video.v9938.VdpV9938;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * This is a machine model for Forth on a 9900 with alternate peripherals
 * @author ejs
 *
 */
public class Forth9900MachineModel implements IMachineModel {

	public static final String ID = "Forth9900";
	
	private MemoryDiskImageDsr memoryDiskDsr;
	
	public Forth9900MachineModel() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getName()
	 */
	@Override
	public String getName() {
		return "FORTH9900 Machine";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#createMachine()
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		return new TI99Machine(settings, this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new Forth9900MemoryModel(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public IVdpChip createVdp(IMachine machine) {
		return new VdpV9938(machine);
	}

	public ISoundChip createSoundChip(IMachine machine) {
		return new MultiSoundTMS9919B(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createCassetteChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ICassetteChip createCassetteChip(IMachine machine) {
		return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineModel#createSpeechChip(v9t9.engine.machine.Machine)
	 */
	@Override
	public ISpeechChip createSpeechChip(IMachine machine) {
		return new SpeechTMS5220(machine, Settings.getSettings(machine), 
				machine.getMemory().getDomain(IMemoryDomain.NAME_SPEECH));
	}
	
	public void defineDevices(final IMachine machine_) {
		machine_.getKeyboardState().registerMapping(KeyboardConstants.KEY_BACKSPACE,
				new int[] { KeyboardConstants.KEY_CONTROL, 'H' });
		
		InternalCruF99 cruAccess = new InternalCruF99(machine_, 0xC0);
		memoryDiskDsr = new MemoryDiskImageDsr(machine_, InternalCruF99.DISK_BASE + cruAccess.getCruBase());
		cruAccess.addIOHandler(memoryDiskDsr);

		machine_.setCru(cruAccess);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<IDeviceSettings> getDeviceSettings(IMachine machine) {
		return Collections.singletonList((IDeviceSettings) memoryDiskDsr);
	}

	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine) {
		return memoryDiskDsr.getDeviceIndicatorProviders();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		return new Cpu9900(machine, machine.getVdp());
	}

	@Override
	public IKeyboardState createKeyboardState(IMachine machine) {
		return new KeyboardState994A(machine);
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
		return EmulatorMachinesData.getDataURL("f9900/");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#isModelCompatible(java.lang.String)
	 */
	@Override
	public boolean isModelCompatible(String machineModel) {
		return getIdentifier().equals(machineModel);
	}
}
