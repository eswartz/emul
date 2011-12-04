/**
 * 
 */
package v9t9.machine.ti99.machine;


import v9t9.common.memory.MemoryModel;
import v9t9.engine.hardware.ISoundChip;
import v9t9.engine.hardware.IVdpChip;
import v9t9.engine.keyboard.KeyboardState;
import v9t9.engine.machine.IMachine;
import v9t9.engine.memory.Vdp9918AMmio;
import v9t9.engine.sound.SoundTMS9919;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import v9t9.machine.common.dsr.emudisk.DiskDirectoryMapper;
import v9t9.machine.common.dsr.emudisk.EmuDiskDsr;
import v9t9.machine.common.dsr.pcode.PCodeDsr;
import v9t9.machine.common.dsr.realdisk.RealDiskImageDsr;
import v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel;

/**
 * @author ejs
 *
 */
public class StandardMachineModel extends BaseTI99MachineModel {

	public static final String ID = "StandardTI994A";
	protected MemoryModel memoryModel;

	public StandardMachineModel() {
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#createMachine()
	 */
	@Override
	public IMachine createMachine() {
		return new TI994A(this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public MemoryModel getMemoryModel() {
		if (memoryModel == null) {
			memoryModel = new TI994AStandardConsoleMemoryModel();
		}
		return memoryModel;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public IVdpChip createVdp(IMachine machine) {
		VdpTMS9918A vdp = new VdpTMS9918A(machine);
		new Vdp9918AMmio(machine.getMemory(), vdp, 0x4000);
		return vdp;
	}
	
	public void defineDevices(IMachine machine_) {
		KeyboardState.backspaceIsCtrlH.setBoolean(false);
		
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.setCruAccess(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr emudsr = new EmuDiskDsr(DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(emudsr);
			RealDiskImageDsr diskdsr = new RealDiskImageDsr(machine, (short) 0x1100);
			machine.getDsrManager().registerDsr(diskdsr);
			
			PCodeDsr pcodedsr = new PCodeDsr(machine);
			machine.getDsrManager().registerDsr(pcodedsr);
		}
	}

	public ISoundChip createSoundChip(IMachine machine) {
		return new SoundTMS9919(machine, null);
	}
}
