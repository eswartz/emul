/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Nov 24, 2008
 *
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemory;
import v9t9.engine.video.v9938.VdpV9938;


/** 
 * V9938 mmio chip entry
 * <p>
 * Models the MSX V9938 card access specified in the DIJIT Systems Advanced Video Processor Card (AVPC) manual
 * @author ejs
 */
public class Vdp9938Mmio extends Vdp9918AMmio {

	private BankedMemoryEntry memoryBank;

	private VdpV9938 v9938;

    public Vdp9938Mmio(IMemory memory, VdpV9938 vdp, int memSize) {
    	super(memory, vdp, adjustMemorySize(memSize));
    	this.v9938 = vdp;
    }
    
    private static int adjustMemorySize(int memorySize) {
		if (memorySize < 0x4000)
			memorySize = 0x4000;
		else if (memorySize < 0x20000)
			memorySize = 0x20000;
		else
			memorySize = 0x30000;
		return memorySize;
	}

    public ByteMemoryArea getMemoryArea() {
    	return fullRamArea;
    }
	protected void initMemory(IMemory memory, int memorySize) {
		memoryBank = new WindowBankedMemoryEntry(
				memory, "VDP RAM",
				videoMemory,
				0x0000, 0x4000,
				fullRamArea);
    	this.memoryEntry = memoryBank;
		memory.addAndMap(memoryBank);
    }

	@Override
	protected int getAbsoluteAddress(int vdpaddr) {
		return vdpaddr + (memoryBank.getCurrentBank() << 14);
	}
	
	@Override
	protected void autoIncrementAddr() {
		vdpaddr = vdpaddr+1 & 0x3fff;
		if (vdpaddr == 0 && v9938.isEnhancedMode()) {
			byte vdpbank = v9938.readVdpReg(14);
			v9938.writeVdpReg(14, (byte) ((vdpbank + 1) & 0x7));
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
    		v9938.writeColorData(val);
			break;
		case 3:
			// indirect data write port
			v9938.writeRegisterIndirect(val);
			break;
    	}
    }

	public BankedMemoryEntry getMemoryBank() {
		return memoryBank;
	}
	
	@Override
	public int getBankAddr() {
		return memoryBank.getCurrentBank() << 14;
	}
	
}
