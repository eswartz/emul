/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Nov 24, 2008
 *
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.hardware.memory.VdpRamArea;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;


/** 
 * V9938 mmio chip entry
 * <p>
 * Models the card specified in the DIJIT Systems Advanced Video Processor Card (AVPC) manual
 * @author ejs
 */
public class Vdp9938Mmio extends Vdp9918AMmio {

	protected byte vdpbank;

	private BankedMemoryEntry memoryBank;

    public Vdp9938Mmio(Memory memory, MemoryDomain videoMemory, int memSize) {
    	super(memory, videoMemory, createBankedMemoryEntry(memory, videoMemory, memSize));
    	memoryBank = (BankedMemoryEntry) memoryEntry;
    }
    
    public static BankedMemoryEntry createBankedMemoryEntry(Memory memory, MemoryDomain domain, int size) {
    	VdpRamArea area = new VdpRamArea(0x20000);
    	MemoryEntry[] banks = new MemoryEntry[size >> 14];
    	for (int bank = 0; bank < banks.length; bank++) {
    		MemoryArea tmp = area.copy();
    		tmp.offset = 0x4000 * bank; 
    		MemoryEntry bankEntry = new MemoryEntry(
    				"VDP RAM bank " + bank, 
    				domain, 0x0000, 0x4000,
    				tmp);
    		banks[bank] = bankEntry;
    	}
		
		return new BankedMemoryEntry(
				memory, "VDP RAM",
				banks);
    }

	@Override
	protected void autoIncrementAddr() {
		vdpaddr = vdpaddr+1 & 0x3fff;
		if (vdpaddr == 0) {
			writeRegister((byte) 14, (byte) ((vdpbank + 1) & memoryBank.getBankCount() - 1));
		}
	}
	
	public void write(int addr, byte val) {
		int port = (addr & 6) >> 1;
    	switch (port) {
    	case 0:
    		writeData(val);
    		break;
    	case 1:
    		writeAddress(val);
    		break;
    	case 2:
			// color data port
			break;
		case 3:
			// indirect data write port
			break;
    	}
    }
	
	@Override
	protected void writeRegister(byte reg, byte regVal) {
		if (reg == 14) {
			vdpbank = (byte) (regVal & memoryBank.getBankCount() - 1);
			memoryBank.selectBank(vdpbank);
			System.out.println("-->vdpbank " + vdpbank);
		} 
		super.writeRegister(reg, regVal);
	}

	public BankedMemoryEntry getMemoryBank() {
		return memoryBank;
	}

	public byte readAbsoluteByte(int vdpaddr) {
		ByteMemoryArea area = (ByteMemoryArea) memoryBank.getBank(0).area;
		return area.memory[vdpaddr];
	}

	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		ByteMemoryArea area = (ByteMemoryArea) memoryBank.getBank(0).area;
		return new ByteMemoryAccess(area.memory, addr);
	}

}
