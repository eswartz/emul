/*
  F99bMachine.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.machine;


import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.hardware.ICruChip;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.demos.DemoContentProvider;
import v9t9.engine.files.image.DiskImageContentProvider;
import v9t9.engine.hardware.BaseCruChip;
import v9t9.engine.machine.MachineBase;
import v9t9.engine.machine.NullFileExecutionHandler;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.machine.ti99.machine.InternalCruF99;

public class F99bMachine extends MachineBase {

	public F99bMachine(ISettingsHandler settings, IMachineModel machineModel) {
		super(settings, machineModel);

		getSettings().get(IKeyboardHandler.settingPasteKeyDelay).setInt(2);
		
		addEmulatorContentProvider(new DemoContentProvider(this));
		addEmulatorContentProvider(new DiskImageContentProvider(this));
	}

	@Override
	protected void init(IMachineModel machineModel) {
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
				cru.triggerInterrupt(InternalCruF99.INT_KBD);
			}
		}
		getCpu().setIdle(false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineBase#createFileExecutionHandler()
	 */
	@Override
	protected IFileExecutionHandler createFileExecutionHandler() {
		return new NullFileExecutionHandler();
	}

}