/**
 * 
 */
package v9t9.machine.f99b.machine;

import java.util.Collections;
import java.util.List;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.memory.MemoryModel;
import v9t9.engine.compiler.NullCompilerStrategy;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.IDeviceIndicatorProvider;
import v9t9.engine.dsr.IDsrSettings;
import v9t9.engine.dsr.realdisk.MemoryDiskImageDsr;
import v9t9.engine.hardware.SoundChip;
import v9t9.engine.hardware.VdpChip;
import v9t9.engine.keyboard.KeyboardState;
import v9t9.engine.machine.IMachine;
import v9t9.engine.machine.Machine;
import v9t9.engine.machine.MachineModel;
import v9t9.engine.memory.Vdp9938Mmio;
import v9t9.engine.sound.MultiSoundTMS9919B;
import v9t9.engine.video.v9938.VdpV9938;
import v9t9.machine.f99b.cpu.CpuF99b;
import v9t9.machine.f99b.cpu.DumpFullReporterF99b;
import v9t9.machine.f99b.cpu.DumpReporterF99b;
import v9t9.machine.f99b.cpu.F99bInstructionFactory;
import v9t9.machine.f99b.interpreter.InterpreterF99b;
import v9t9.machine.f99b.memory.F99bMemoryModel;

/**
 * This is a machine model for F99b
 * @author ejs
 *
 */
public class F99bMachineModel implements MachineModel {

	public static final String ID = "Forth99B";
	
	private F99bMemoryModel memoryModel;
	private VdpV9938 vdp;

	private MemoryDiskImageDsr memoryDiskDsr;
	
	public F99bMachineModel() {
		memoryModel = new F99bMemoryModel();
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
		return new F99bMachine(this);
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
	public VdpChip createVdp(IMachine machine) {
		//VdpV9938.settingMsxClockDivisor.setInt(1);
		vdp = new VdpV9938(machine);
		new Vdp9938Mmio(machine.getMemory(), vdp, 0x20000);
		return vdp;
	}

	public SoundChip createSoundProvider(IMachine machine) {
		return new MultiSoundTMS9919B(machine);
	}
	
	public void defineDevices(final IMachine machine_) {
		KeyboardState.backspaceIsCtrlH.setBoolean(true);
		
		memoryDiskDsr = new MemoryDiskImageDsr(machine_, InternalCruF99.DISK_BASE);

		InternalCruF99 cruAccess = new InternalCruF99(machine_, machine_.getKeyboardState());
		cruAccess.addIOHandler(memoryDiskDsr);

		machine_.setCruAccess(cruAccess);
		//machine_.getDsrManager().registerDsr(dsr);
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
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<IDsrSettings> getDsrSettings(IMachine machine) {
		return Collections.singletonList((IDsrSettings) memoryDiskDsr);
	}

	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine) {
		return memoryDiskDsr.getDeviceIndicatorProviders();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public ICpu createCPU(Machine machine) {
		return new CpuF99b(machine, 1000 / machine.getCpuTicksPerSec(), machine.getVdp());
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getInstructionFactory()
	 */
	@Override
	public IRawInstructionFactory getInstructionFactory() {
		return new F99bInstructionFactory();
	}

	@Override
	public Executor createExecutor(ICpu cpu, ICpuMetrics metrics) {
		return new Executor(cpu, metrics, 
				new InterpreterF99b(cpu.getMachine()),
				null,
				new NullCompilerStrategy(),
				new DumpFullReporterF99b((CpuF99b) cpu, null), new DumpReporterF99b((CpuF99b) cpu));
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
