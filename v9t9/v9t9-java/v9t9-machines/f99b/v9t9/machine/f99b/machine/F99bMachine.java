package v9t9.machine.f99b.machine;


import v9t9.base.settings.ISettingSection;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.hardware.BaseCruAccess;
import v9t9.engine.hardware.ICruAccess;
import v9t9.engine.machine.MachineBase;
import v9t9.engine.machine.MachineModel;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.machine.f99b.cpu.CpuF99b;

public class F99bMachine extends MachineBase {

	public F99bMachine(MachineModel machineModel) {
		super(machineModel);
		keyboardState.setPasteKeyDelay(6);
	}

	@Override
	protected void init(MachineModel machineModel) {
		settingModuleList.setString("");
		
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
		if (keyboardState.anyKeyPressed()) {
			ICruAccess cru = getCruAccess();
			if (cru instanceof BaseCruAccess) {
				cru.triggerInterrupt(CpuF99b.INT_KBD);
			}
		}
		getCpu().setIdle(false);
	}

}