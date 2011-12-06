/**
 * 
 */
package v9t9.machine.ti99.machine;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.settings.Settings;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.VdpMmio;
import v9t9.engine.memory.WindowBankedMemoryEntry;
import v9t9.engine.sound.MultiSoundTMS9919B;
import v9t9.engine.video.v9938.VdpV9938;
import v9t9.machine.common.dsr.emudisk.DiskDirectoryMapper;
import v9t9.machine.common.dsr.emudisk.EmuDiskDsr;
import v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel;
import v9t9.machine.ti99.memory.V9t9EnhancedConsoleMemoryModel;

/**
 * This is an enhanced machine model that has a more regular memory model as well.
 * @author ejs
 *
 */
public class Enhanced48KForthTI994AMachineModel extends BaseTI99MachineModel {

	public static final String ID = "Enhanced48KForthTI994A";
	
	private BankedMemoryEntry cpuBankedVideo;
	private boolean vdpCpuBanked;
	//protected MemoryEntry currentMemory;
	
	public Enhanced48KForthTI994AMachineModel() {
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
	public IMachine createMachine(ISettingsHandler settings) {
		return new TI994A(settings, this);
	}
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new V9t9EnhancedConsoleMemoryModel(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public IVdpChip createVdp(IMachine machine) {
		return new VdpV9938(machine);
	}

	public ISoundChip createSoundChip(IMachine machine) {
		return new MultiSoundTMS9919B(machine);
	}
	
	public void defineDevices(final IMachine machine_) {
		Settings.get(machine_, IKeyboardState.settingBackspaceIsCtrlH).setBoolean(true);
		
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.setCru(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr dsr = new EmuDiskDsr(Settings.getSettings(machine_),
					DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(dsr);
			
			defineCpuVdpBanks(machine);
		}
	}
	
	private void defineCpuVdpBanks(final TI99Machine machine) {
		final IVdpChip vdp = machine.getVdp();
		VdpMmio vdpMmio = ((TI994AStandardConsoleMemoryModel) machine.getMemory().getModel()).getVdpMmio();
		
		cpuBankedVideo = new WindowBankedMemoryEntry(machine.getMemory(),
				"CPU VDP Bank", 
				machine.getConsole(),
				0xA000,
				0x4000,
				vdpMmio.getMemoryArea()) {
			@Override
			public void writeByte(int addr, byte val) {
				super.writeByte(addr, val);
				vdp.touchAbsoluteVdpMemory((addr & 0x3fff) + getBankOffset());
			}
			@Override
			public void writeWord(int addr, short val) {
				super.writeWord(addr, val);
				vdp.touchAbsoluteVdpMemory((addr & 0x3fff) + getBankOffset());
			}
		};

		machine.getCruManager().add(0x1402, 1, new ICruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					vdpCpuBanked = true;
					//currentMemory = machine.getMemory().map.lookupEntry(machine.getConsole(), 0xc000);
					cpuBankedVideo.getDomain().mapEntry(cpuBankedVideo);
				} else {
					vdpCpuBanked = false;
					cpuBankedVideo.getDomain().unmapEntry(cpuBankedVideo);
					//if (currentMemory != null)
					//	currentMemory.map();
				}
				return 0;
			}
			
		});
		ICruWriter bankSelector = new ICruWriter() {

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
