package v9t9.machine.ti99.machine;


import ejs.base.settings.ISettingSection;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.machine.MachineBase;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.SpeechMmio;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.engine.memory.VdpMmio;
import v9t9.machine.ti99.dsr.DsrManager;
import v9t9.machine.ti99.memory.BaseTI994AMemoryModel;

public class TI99Machine extends MachineBase {

	public static final String KEYBOARD_MODE_TI994A = "ti994a";
	public static final String KEYBOARD_MODE_TI994 = "ti994";
	public static final String KEYBOARD_MODE_LEFT = "left";
	public static final String KEYBOARD_MODE_RIGHT = "right";
	public static final String KEYBOARD_MODE_PASCAL = "pascal";
	
	private CruManager cruManager;
	protected DsrManager dsrManager;
	
	public TI99Machine(ISettingsHandler settings, IMachineModel machineModel) {
		super(settings, machineModel);
		
		getSettings().get(IKeyboardHandler.settingPasteKeyDelay).setInt(3);
	}

	@Override
	protected void init(IMachineModel machineModel) {
		super.init(machineModel);

		cruManager = new CruManager();
		dsrManager = new DsrManager(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineBase#setNotRunning()
	 */
	@Override
	public void setNotRunning() {
		super.setNotRunning();
		 if (dsrManager != null)
				dsrManager.dispose();
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
		if (dsrManager != null)
			dsrManager.loadState(section.getSection("DSRs"));
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.Machine#doSaveState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	protected void doSaveState(ISettingSection settings) {
		super.doSaveState(settings);
		getMemoryModel().getGplMmio().saveState(settings.addSection("GPL"));
		if (dsrManager != null)
			dsrManager.saveState(settings.addSection("DSRs"));

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSoundMmio()
	 */
	public v9t9.engine.memory.SoundMmio getSoundMmio() {
	    return ((BaseTI994AMemoryModel) memoryModel).soundMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getVdpMmio()
	 */
	public VdpMmio getVdpMmio() {
	    return ((BaseTI994AMemoryModel) memoryModel).vdpMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getGplMmio()
	 */
	public GplMmio getGplMmio() {
	    return ((BaseTI994AMemoryModel) memoryModel).gplMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSpeechMmio()
	 */
	public SpeechMmio getSpeechMmio() {
		return ((BaseTI994AMemoryModel) memoryModel).speechMmio;
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

	public CruManager getCruManager() {
		return cruManager;
	}
	
	public IDsrManager getDsrManager() {
		return dsrManager;
	}
}