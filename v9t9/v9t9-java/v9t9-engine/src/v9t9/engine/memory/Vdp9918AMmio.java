/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine.memory;


import v9t9.base.utils.HexUtils;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.hardware.IVdpChip;
import v9t9.engine.video.tms9918a.VdpTMS9918A;



/** VDP chip entry
 * @author ejs
 */
public class Vdp9918AMmio extends VdpMmio {

	protected IMemoryEntry memoryEntry;
	protected final IMemoryDomain videoMemory;

	/**
     * @param machine
     */
    public Vdp9918AMmio(IMemory memory, IVdpChip vdp, int memorySize) {
    	super(new VdpRamArea(memorySize));
    	fullRamArea.setHandler(vdp);
		this.videoMemory = vdp.getVideoMemory();
		initMemory(memory, memorySize);
		vdp.setVdpMmio(this);
		setVdpHandler(vdp);
    }
    
    protected void initMemory(IMemory memory, int memorySize) {
    	MemoryEntry memoryEntry = new MemoryEntry(
    			"VDP RAM", videoMemory, 0x0000, memorySize, 
				fullRamArea);
    	this.memoryEntry = memoryEntry;
		memory.addAndMap(memoryEntry);
    }

	protected int vdpaddr;
	protected boolean vdpaddrflag;
	protected byte vdpreadahead;
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioReader#read
     */
    public byte read(int addr) {
    	byte ret;

    	vdpaddrflag = false;
		if ((addr & 2) != 0) {
		    ret = readStatus();
		} else {					
			ret = readData();
		}
    	return ret;
    }

	protected byte readData() {
		/* >8800, memory read */
		byte ret;
		ret = vdpreadahead;
		vdpreadahead = videoMemory.readByte(vdpaddr);
		autoIncrementAddr();
		return ret;
	}

	protected byte readStatus() {
		byte ret = vdpHandler.readVdpStatus();
		return ret;
	}

	/**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	if ((addr & 2) != 0) {
    		writeAddress(val);
    	} else {					
    	    writeData(val);
    	}    
        
    }

	protected void writeData(byte val) {
		/* >8C00, data write */
		
		/* this flag is used to verify that the VDP
		   address was written as >4000 + vdpaddr.
		   If not, then writing to it functions as
		   a read-before-write. */
		vdpaddrflag = false;

		//byte oldval = videoMemory.flatReadByte(vdpaddr);
		videoMemory.writeByte(vdpaddr, val);
		
		if ((vdpaddr & 0xf) == 0 
				&& ICpu.settingDumpFullInstructions.getBoolean()
				&& VdpTMS9918A.settingDumpVdpAccess.getBoolean()) {
			VdpTMS9918A.log("Address: " + HexUtils.toHex4(vdpaddr));
		}
		
		autoIncrementAddr();
		vdpreadahead = val;
		
	}

	protected int getAbsoluteAddress(int vdpaddr) {
		return vdpaddr;
	}

	protected void autoIncrementAddr() {
		vdpaddr = vdpaddr+1 & 0x3fff;
		
	}

	protected void writeAddress(byte val) {
	    /* >8C02, address write */

		if (vdpaddrflag) {
			vdpaddr = (vdpaddr & 0xff) | (val << 8);
		} else {
			vdpaddr = (vdpaddr & 0xff00) | (val & 0xff);
		}
		if ((vdpaddrflag = !vdpaddrflag) == false) {
			if (ICpu.settingDumpFullInstructions.getBoolean()
					&& VdpTMS9918A.settingDumpVdpAccess.getBoolean()) {
				VdpTMS9918A.log("Address: " + HexUtils.toHex4(vdpaddr));
			}
			if ((vdpaddr & 0x8000) != 0) {
				writeRegAddr(vdpaddr);
				vdpaddr &= 0x3fff;
			} else if ((vdpaddr & 0x4000) != 0) {
				vdpaddr &= 0x3fff;
			} else {
				// read ahead one byte
				vdpreadahead = videoMemory.readByte(vdpaddr);
				autoIncrementAddr();
			}
		}
	}

	protected void writeRegAddr(int addr) {
		byte regVal = (byte) (addr & 0xff00 >> 8);
		byte regNum = (byte) ((addr >> 8) & 0x3f);
		writeRegister(regNum, regVal);
	}

	protected void writeRegister(byte reg, byte regVal) {
		if (vdpHandler != null)
			vdpHandler.writeVdpReg(reg, regVal);
	}
    
    public void setAddr(int addr) {
        if ((addr & 0x8000) != 0) {
			writeRegAddr(addr);
		}
        vdpaddr = (short) (addr & 0x3fff);
        vdpaddrflag = false;
    }
    
    public int getAddr() {
        return vdpaddr;
    }
    
    /* (non-Javadoc)
     * @see v9t9.emulator.hardware.memory.mmio.VdpMmio#getMemoryBank()
     */
    @Override
    public BankedMemoryEntry getMemoryBank() {
    	return null;
    }
}
