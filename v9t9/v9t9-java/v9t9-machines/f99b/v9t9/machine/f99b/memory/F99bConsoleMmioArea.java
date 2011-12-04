/**
 * 
 */
package v9t9.machine.f99b.memory;

import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.IMachine;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.machine.f99b.machine.InternalCruF99;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;

/**
 * F99 memory-mapped I/O area
 * @author ejs
 *
 */
public class F99bConsoleMmioArea extends ConsoleMmioArea  {
	
	// The VDP ports are intended to mostly fit in 1-byte IlitX instructions
	public static final int VDPRD = 0x0;
	public static final int VDPST = 0x2;
	public static final int VDPWD = 0x4;
	public static final int VDPWA = 0x6;
	public static final int VDPWAL = 0x7;
	public static final int VDPCL = 0x8;
	public static final int VDPWI = 0xA;
	
	private static final int[] f99ToVdpPort = { 0, 1, 2, 3, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
	
	
	public static final int GPLRD = 0x10;
	public static final int GPLRA = 0x12;
	public static final int GPLWD = 0x14;
	public static final int GPLWA = 0x16;
	public static final int SPCHWT = 0x18;
	public static final int SPCHRD = 0x1A;
	public static final int KEYRD = 0x20;
	public static final int KEYWR = 0x22;
	public static final int SOUND = 0x40;	// 0x20!
	
	// character outlet
	public static final int DBG = 0xff;
	
	public static final int CRU_BASE = 0x80;
	public static final int CRU_END = 0x100;
	
	private final IMachine machine;
		
	F99bConsoleMmioArea(IMachine machine) {
		super(0);
		this.machine = machine;
    };
    
    @Override
	public boolean hasReadAccess() {
		return true;
	}

    @Override
    public void writeByte(IMemoryEntry entry, int addr, byte val) {
    	writeMmio(addr, val);
    }

	@Override
    public void writeWord(IMemoryEntry entry, int addr, short val) {
		if (addr == VDPWA) {
			writeByte(entry, VDPWA, (byte) (val & 0xff));
			writeByte(entry, VDPWA, (byte) (val >> 8));
		} else if (addr == GPLWA) {
			writeByte(entry, GPLWA, (byte) (val >> 8));
			writeByte(entry, GPLWA, (byte) (val & 0xff));
		} else {
	    	writeByte(entry, addr, (byte) (val >> 8));
		}
    }


	@Override
	public byte readByte(IMemoryEntry entry, int addr) {
		return readMmio(addr);
	}
	
	@Override
	public short readWord(IMemoryEntry entry, int addr) {
		if (addr == GPLRA) {
			byte hi = readByte(entry, addr);
			byte lo = readByte(entry, addr);
			return (short) ((hi << 8) | (lo & 0xff));
		} else {
			return (short) (readByte(entry, addr) << 8);
			
		}
	}

    private void writeMmio(int addr, byte val) {

    	if (addr >= SOUND && addr < SOUND + 0x20) {
    		getTIMemoryModel().getSoundMmio().write(addr, val);
    	}
    	else {
	    	switch (addr) {
	    	case VDPWD:
	    	case VDPCL:
	    	case VDPWI:
	    	case VDPWA:
	    		getTIMemoryModel().getVdpMmio().write(f99ToVdpPort[addr], val);
	    		break;
	    	case GPLWA:
	    	case GPLWD:
	    		getTIMemoryModel().getGplMmio().write(addr, val);
	    		break;
	    	case SPCHWT:
	    		getTIMemoryModel().getSpeechMmio().write(addr, val);
	    		break;
	    		
	    	case DBG:
	    		System.out.print((char) val);
	    		break;
    		default:
    			if (addr >= CRU_BASE && addr < CRU_END) {
    				((InternalCruF99) machine.getCru()).handleWrite(addr, val);
    			}
    			break;
	    	}
    	}
	}

	private TIMemoryModel getTIMemoryModel() {
		return (TIMemoryModel) machine.getMemoryModel();
	}

	private byte readMmio(int addr) {
		switch (addr) {
    	case VDPRD:
    	case VDPST:
    		return getTIMemoryModel().getVdpMmio().read(addr);
    	case GPLRD:
    	case GPLRA:
    		return getTIMemoryModel().getGplMmio().read(addr);
    	case SPCHRD:
    		return getTIMemoryModel().getSpeechMmio().read(addr);
    	default:
			if (addr >= CRU_BASE && addr < CRU_END) {
				return ((InternalCruF99) machine.getCru()).handleRead(addr);
			}
			break;
    	}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.WordMemoryArea#getSize()
	 */
	@Override
	protected int getSize() {
		return 0x400;
	}
}
