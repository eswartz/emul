/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.F99MemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.emulator.hardware.sound.MultiSoundTMS9919B;
import v9t9.emulator.runtime.compiler.NullCompilerStrategy;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.DumpFullReporterF99;
import v9t9.emulator.runtime.cpu.DumpReporterF99;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.emulator.runtime.interpreter.InterpreterF99;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;
import v9t9.tools.asm.assembler.IInstructionFactory;

/**
 * This is a machine model for F99
 * @author ejs
 *
 */
public class F99MachineModel implements MachineModel {

	public static final String ID = "Forth99";
	
	private F99MemoryModel memoryModel;
	private Vdp9938Mmio vdpMmio;
	private VdpV9938 vdp;
	
	public F99MachineModel() {
		memoryModel = new F99MemoryModel();
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
	public Machine createMachine() {
		return new F99Machine(this);
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
		vdp = new VdpV9938(machine);
		vdpMmio = new Vdp9938Mmio(machine.getMemory(), vdp, 0x20000);
		return vdp;
	}

	public SoundProvider createSoundProvider(Machine machine) {
		return new MultiSoundTMS9919B(machine);
	}
	
	public void defineDevices(final Machine machine_) {
		/*
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.getCpu().setCruAccess(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr dsr = new EmuDiskDsr(DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(dsr);
			
			defineCpuVdpBanks(machine);
		}
		*/
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public Cpu createCPU(Machine machine) {
		return new CpuF99(machine, 1000 / machine.getCpuTicksPerSec(), machine.getVdp());
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getInstructionFactory()
	 */
	@Override
	public IInstructionFactory getInstructionFactory() {
		return null;
	}

	@Override
	public Executor createExecutor(Cpu cpu, CpuMetrics metrics) {
		return new Executor(cpu, metrics, 
				new InterpreterF99(cpu.getMachine()),
				new NullCompilerStrategy(),
				new DumpFullReporterF99((CpuF99) cpu, null),
				new DumpReporterF99((CpuF99) cpu));
	}
	/*
	private void defineCpuVdpBanks(final TI99Machine machine) {
		
		cpuBankedVideo = new WindowBankedMemoryEntry(machine.getMemory(),
				"CPU VDP Bank", 
				machine.getConsole(),
				0xA000,
				0x4000,
				vdpMmio.getMemoryArea()) {
			@Override
			public void writeByte(int addr, byte val) {
				super.writeByte(addr, val);
				vdp.touchAbsoluteVdpMemory((addr & 0x3fff) + getBankOffset(), val);
			}
			@Override
			public void writeWord(int addr, short val) {
				super.writeWord(addr, val);
				vdp.touchAbsoluteVdpMemory((addr & 0x3fff) + getBankOffset(), (byte) (val >> 8));
			}
		};

		machine.getCruManager().add(0x1402, 1, new CruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					vdpCpuBanked = true;
					//currentMemory = machine.getMemory().map.lookupEntry(machine.getConsole(), 0xc000);
					cpuBankedVideo.domain.mapEntry(cpuBankedVideo);
				} else {
					vdpCpuBanked = false;
					cpuBankedVideo.domain.unmapEntry(cpuBankedVideo);
					//if (currentMemory != null)
					//	currentMemory.map();
				}
				return 0;
			}
			
		});
		CruWriter bankSelector = new CruWriter() {

			public int write(int addr, int data, int num) {
				// independent banking from VDP
				if (vdpCpuBanked) {
					int currentBank = cpuBankedVideo.getCurrentBank();
					int bit = (addr - 0x1404) >> 1;
					currentBank = (currentBank & ~(1 << bit)) | (data << bit);
					cpuBankedVideo.selectBank(currentBank);
				}
				return 0;
			}
			
		};
		machine.getCruManager().add(0x1404, 1, bankSelector);
		machine.getCruManager().add(0x1406, 1, bankSelector);
		machine.getCruManager().add(0x1408, 1, bankSelector);
	}
	*/
}
