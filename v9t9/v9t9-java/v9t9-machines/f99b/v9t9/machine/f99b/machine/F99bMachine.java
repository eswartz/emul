/*
  F99bMachine.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.machine;


import java.util.Collections;

import ejs.base.settings.ISettingSection;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.hardware.ICruChip;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.modules.IModuleManager;
import v9t9.common.settings.Settings;
import v9t9.engine.hardware.BaseCruChip;
import v9t9.engine.machine.MachineBase;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.machine.f99b.cpu.CpuF99b;

public class F99bMachine extends MachineBase {

	public F99bMachine(ISettingsHandler settings, IMachineModel machineModel) {
		super(settings, machineModel);

		getSettings().get(IKeyboardHandler.settingPasteKeyDelay).setInt(2);
	}

	@Override
	protected void init(IMachineModel machineModel) {
		Settings.get(this, IModuleManager.settingModuleList).setList(Collections.emptyList());
		
		super.init(machineModel);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.Machine#getMemoryModel()
	 */
	@Override
	public TIMemoryModel getMemoryModel() {
		return (TIMemoryModel) super.getMemoryModel();
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.Machine#doLoadState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	protected void doLoadState(ISettingSection section) {
		super.doLoadState(section);
		getMemoryModel().getGplMmio().loadState(section.getSection("GPL"));
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.Machine#doSaveState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	protected void doSaveState(ISettingSection settings) {
		super.doSaveState(settings);
		getMemoryModel().getGplMmio().saveState(settings.addSection("GPL"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getGplMemoryDomain()
	 */
	public IMemoryDomain getGplMemoryDomain() {
		return memory.getDomain(IMemoryDomain.NAME_GRAPHICS);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSpeechMemoryDomain()
	 */
	public IMemoryDomain getSpeechMemoryDomain() {
		return memory.getDomain(IMemoryDomain.NAME_SPEECH);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getVdpMemoryDomain()
	 */
	public IMemoryDomain getVdpMemoryDomain() {
		return memory.getDomain(IMemoryDomain.NAME_VIDEO);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.Machine#keyStateChanged()
	 */
	@Override
	public void keyStateChanged() {
		super.keyStateChanged();
		if (getKeyboardHandler().anyKeyAvailable()) {
			ICruChip cru = getCru();
			if (cru instanceof BaseCruChip) {
				cru.triggerInterrupt(CpuF99b.INT_KBD);
			}
		}
		getCpu().setIdle(false);
	}

}