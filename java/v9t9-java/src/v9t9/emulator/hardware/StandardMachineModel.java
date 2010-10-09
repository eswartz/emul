/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.emudisk.*;
import v9t9.emulator.hardware.dsrs.pcode.PCodeDsr;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr;
import v9t9.emulator.hardware.memory.*;
import v9t9.emulator.hardware.memory.mmio.Vdp9918AMmio;
import v9t9.emulator.hardware.sound.SoundTMS9919;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;

/**
 * @author ejs
 *
 */
public class StandardMachineModel extends BaseTI99MachineModel {

	private TI994AStandardConsoleMemoryModel memoryModel;

	public StandardMachineModel() {
		memoryModel = new TI994AStandardConsoleMemoryModel();
		ExpRamArea.settingExpRam.setBoolean(true);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public MemoryModel getMemoryModel() {
		return memoryModel;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public VdpHandler createVdp(Machine machine) {
		VdpTMS9918A vdp = new VdpTMS9918A(machine);
		new Vdp9918AMmio(machine.getMemory(), vdp, 0x4000);
		return vdp;
	}
	
	public void defineDevices(Machine machine_) {
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.getCpu().setCruAccess(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr emudsr = new EmuDiskDsr(DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(emudsr);
			DiskImageDsr diskdsr = new DiskImageDsr(machine);
			machine.getDsrManager().registerDsr(diskdsr);
			
			PCodeDsr pcodedsr = new PCodeDsr(machine);
			machine.getDsrManager().registerDsr(pcodedsr);
		}
	}

	public SoundProvider createSoundProvider(Machine machine) {
		return new SoundTMS9919(machine, null);
	}
}
