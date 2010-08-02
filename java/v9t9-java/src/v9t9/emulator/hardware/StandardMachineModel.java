/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper;
import v9t9.emulator.hardware.dsrs.emudisk.EmuDiskDsr;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr;
import v9t9.emulator.hardware.memory.ExpRamArea;
import v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9918AMmio;
import v9t9.emulator.hardware.sound.SoundTMS9919;
import v9t9.emulator.runtime.compiler.CodeBlockCompilerStrategy;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.DumpFullReporter9900;
import v9t9.emulator.runtime.cpu.DumpReporter9900;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.emulator.runtime.interpreter.Interpreter9900;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;

/**
 * @author ejs
 *
 */
public class StandardMachineModel implements MachineModel {

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
		}
	}

	public SoundProvider createSoundProvider(Machine machine) {
		return new SoundTMS9919(machine, null);
	}
	
	@Override
	public Executor createExecutor(Cpu cpu, CpuMetrics metrics) {
		return new Executor(cpu, metrics, 
				new Interpreter9900((TI99Machine) cpu.getMachine()),
				new CodeBlockCompilerStrategy(),
				new DumpFullReporter9900((Cpu9900) cpu),
				new DumpReporter9900((Cpu9900) cpu));
	}
}
