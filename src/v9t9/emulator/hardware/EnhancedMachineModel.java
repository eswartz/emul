/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.emulator.hardware.dsrs.EmuDiskDSR;
import v9t9.emulator.hardware.memory.EnhancedConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.memory.MemoryArea.AreaWriteByte;

/**
 * @author ejs
 *
 */
public class EnhancedMachineModel implements MachineModel {

	private EnhancedConsoleMemoryModel memoryModel;
	private Vdp9938Mmio vdpMmio;
	private BankedMemoryEntry cpuBankedVideo;
	private VdpV9938 vdp;
	private boolean vdpCpuBanked;
	protected MemoryEntry currentMemory;
	
	public EnhancedMachineModel() {
		memoryModel = new EnhancedConsoleMemoryModel();
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
		vdp = new VdpV9938(memoryModel.VIDEO);
		vdpMmio = new Vdp9938Mmio(machine.getMemory(), vdp, 0x20000);
		return vdp;
	}

	public void defineDevices(final Machine machine) {
		EmuDiskDSR dsr = new EmuDiskDSR(machine);
		machine.getDSRManager().registerDsr(dsr);
		
		defineCpuVdpBanks(machine);
	}
	
	private void defineCpuVdpBanks(final Machine machine) {
		ByteMemoryArea vdpMemory = vdpMmio.getMemoryArea();
		MemoryEntry[] vdpCpuBanks = new MemoryEntry[vdpMemory.memory.length >> 14];
    	for (int bank = 0; bank < vdpCpuBanks.length; bank++) {
    		MemoryArea tmp = new ByteMemoryArea(8, vdpMemory.memory, bank << 14);
    		MemoryEntry bankEntry = new MemoryEntry(
    				"CPU VDP RAM bank " + bank, 
    				machine.getConsole(), 0xC000, 0x4000,
    				tmp);
    		tmp.areaWriteByte = new AreaWriteByte() {

				public void writeByte(MemoryArea area, int address, byte val) {
					area.flatWriteByte(address, val);
					vdp.touchAbsoluteVdpMemory((address & 0x3fff) + (cpuBankedVideo.getCurrentBank() << 14), val);
				}
    			
    		};
    		vdpCpuBanks[bank] = bankEntry;
    	}
		
		cpuBankedVideo = new BankedMemoryEntry(machine.getMemory(),
				"CPU VDP Bank", vdpCpuBanks);

		machine.getCruManager().add(0x1400, 1, new CruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					vdpCpuBanked = true;
					currentMemory = machine.getMemory().map.lookupEntry(machine.getConsole(), 0xc000);
					cpuBankedVideo.map();
				} else {
					vdpCpuBanked = false;
					cpuBankedVideo.unmap();
					if (currentMemory != null)
						currentMemory.map();
				}
				return 0;
			}
			
		});
		CruWriter bankSelector = new CruWriter() {

			public int write(int addr, int data, int num) {
				// independent banking from VDP
				if (vdpCpuBanked) {
					int currentBank = cpuBankedVideo.getCurrentBank();
					int bit = (addr - 0x1402) >> 1;
					currentBank = (currentBank & ~(1 << bit)) | (data << bit);
					cpuBankedVideo.selectBank(currentBank);
				}
				return 0;
			}
			
		};
		machine.getCruManager().add(0x1402, 1, bankSelector);
		machine.getCruManager().add(0x1404, 1, bankSelector);
		machine.getCruManager().add(0x1406, 1, bankSelector);
	}

}
