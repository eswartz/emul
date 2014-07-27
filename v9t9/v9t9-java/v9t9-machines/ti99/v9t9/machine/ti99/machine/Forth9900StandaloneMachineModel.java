/*
  F99bMachineModel.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.ICassetteChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
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
import v9t9.machine.f99b.machine.InternalCruF99;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * This is a machine model for F99b
 * @author ejs
 *
 */
public class Forth9900StandaloneMachineModel extends Forth9900MachineModel {

	public static final String ID = "Forth9900Standalone";
	
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
		return "FORTH9900 Machine (Standalone)";
	}
	
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new Forth9900StandaloneMemoryModel(machine);
	}
}
