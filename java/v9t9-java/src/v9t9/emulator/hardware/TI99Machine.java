package v9t9.emulator.hardware;

import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.DsrManager9900;
import v9t9.emulator.hardware.dsrs.IDsrManager;
import v9t9.emulator.hardware.memory.StandardConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.SpeechMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.CruHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryDomain;

public class TI99Machine extends Machine {

	private CruManager cruManager;

	public TI99Machine(MachineModel machineModel) {
		super(machineModel);
	}

	@Override
	protected void init(MachineModel machineModel) {
		super.init(machineModel);

		cruManager = new CruManager();
		dsrManager = new DsrManager9900(this);
	}
	
	@Override
	public void setNotRunning() {
		super.setNotRunning();
		dsrManager.dispose();
	}
	
	@Override
	protected void doSaveState(ISettingSection settings) {
		super.doSaveState(settings);
		dsrManager.saveState(settings.addSection("DSRs"));
	}

	@Override
	protected void doLoadState(ISettingSection section) {
		super.doLoadState(section);
		dsrManager.loadState(section.getSection("DSRs"));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSoundMmio()
	 */
	public v9t9.emulator.hardware.memory.mmio.SoundMmio getSoundMmio() {
	    return ((StandardConsoleMemoryModel) memoryModel).soundMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getVdpMmio()
	 */
	public VdpMmio getVdpMmio() {
	    return getVdp().getVdpMmio();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getGplMmio()
	 */
	public GplMmio getGplMmio() {
	    return ((StandardConsoleMemoryModel) memoryModel).gplMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSpeechMmio()
	 */
	public SpeechMmio getSpeechMmio() {
		return ((StandardConsoleMemoryModel) memoryModel).speechMmio;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getGplMemoryDomain()
	 */
	public MemoryDomain getGplMemoryDomain() {
		return memory.getDomain("GRAPHICS");
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getSpeechMemoryDomain()
	 */
	public MemoryDomain getSpeechMemoryDomain() {
		return memory.getDomain("SPEECH");
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.TI99Machine#getVdpMemoryDomain()
	 */
	public MemoryDomain getVdpMemoryDomain() {
		return memory.getDomain("VIDEO");
	}

	public CruManager getCruManager() {
		return cruManager;
	}


	public CruHandler getCru() {
		return cruManager;
	}

}