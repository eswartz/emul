package v9t9.emulator.hardware;

import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.DsrManager9900;
import v9t9.emulator.hardware.dsrs.IDsrManager;
import v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.SpeechMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.CruHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryDomain;

public class MFP201Machine extends Machine {

	public MFP201Machine() {
		super(new MFP201MachineModel());
	}

	@Override
	protected void init(MachineModel machineModel) {
		super.init(machineModel);
		dsrManager = null;
	}
	
}