/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.emulator.common.IMachine;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper;
import v9t9.emulator.hardware.dsrs.emudisk.EmuDiskDsr;
import v9t9.emulator.hardware.memory.V9t9EnhancedConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.emulator.hardware.sound.SoundTMS9919;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.memory.WindowBankedMemoryEntry;

/**
 * @author ejs
 *
 */
public class EnhancedCompatibleMachineModel extends BaseTI99MachineModel {

	public static final String ID = "EnhancedCompatibleTI994A";
	
	private V9t9EnhancedConsoleMemoryModel memoryModel;
	private Vdp9938Mmio vdpMmio;
	private BankedMemoryEntry cpuBankedVideo;
	private VdpV9938 vdp;
	private boolean vdpCpuBanked;
	//protected MemoryEntry currentMemory;
	
	public EnhancedCompatibleMachineModel() {
		memoryModel = new V9t9EnhancedConsoleMemoryModel();
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
		return memoryModel;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public VdpHandler createVdp(IMachine machine) {
		vdp = new VdpV9938(machine);
		vdpMmio = new Vdp9938Mmio(machine.getMemory(), vdp, 0x20000);
		return vdp;
	}
	
	public SoundProvider createSoundProvider(IMachine machine) {
		return new SoundTMS9919(machine, null);
	}

	public void defineDevices(final IMachine machine_) {
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.getCpu().setCruAccess(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr dsr = new EmuDiskDsr(DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(dsr);
			
			defineCpuVdpBanks(machine);
		}
	}
	
	private void defineCpuVdpBanks(final TI99Machine machine) {
		
		cpuBankedVideo = new WindowBankedMemoryEntry(machine.getMemory(),
				"CPU VDP Bank", 
				machine.getConsole(),
				0xC000,
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
}
