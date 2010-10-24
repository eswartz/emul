/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.engine.memory.*;

/**
 * F99 memory-mapped I/O area
 * @author ejs
 *
 */
public class F99ConsoleMmioArea extends ConsoleMmioArea  {
	
	public static final int VDPRD = 0x0;
	public static final int VDPST = 0x2;
	public static final int VDPWD = 0x8;
	public static final int VDPWA = 0xA;
	public static final int VDPCL = 0xC;
	public static final int VDPWI = 0xE;
	public static final int GPLRD = 0x10;
	public static final int GPLRA = 0x12;
	public static final int GPLWD = 0x14;
	public static final int GPLWA = 0x16;
	public static final int SPCHWT = 0x18;
	public static final int SPCHRD = 0x1A;
	public static final int KEYRD = 0x20;
	public static final int KEYWR = 0x22;
	public static final int SOUND = 0x40;	// 0x20!
	
	private final Machine machine;
		
	F99ConsoleMmioArea(Machine machine) {
		super(0);
		this.machine = machine;
    };
    
    @Override
	public boolean hasReadAccess() {
		return true;
	}

    @Override
    public void writeByte(MemoryEntry entry, int addr, byte val) {
    	if ((addr & 1) != 0)
    		return;
    	
    	writeMmio(addr, val);
    }

	@Override
    public void writeWord(MemoryEntry entry, int addr, short val) {
    	writeByte(entry, addr, (byte) (val >> 8));
    }


	@Override
	public byte readByte(MemoryEntry entry, int addr) {
    	if ((addr & 1) != 0)
    		return 0;
		return readMmio(addr);
	}
	
	@Override
	public short readWord(MemoryEntry entry, int addr) {
		return (short) (readByte(entry, addr) << 8);
	}

    private void writeMmio(int addr, byte val) {
    	switch (addr) {
    	case VDPWD:
    	case VDPWA:
    	case VDPCL:
    	case VDPWI:
    		getTIMemoryModel().getVdpMmio().write(addr, val);
    		break;
    	case GPLWA:
    	case GPLWD:
    		getTIMemoryModel().getGplMmio().write(addr, val);
    		break;
    	case SPCHWT:
    		getTIMemoryModel().getSpeechMmio().write(addr, val);
    		break;
    	}

    	if (addr >= SOUND && addr < SOUND + 0x20) {
    		getTIMemoryModel().getSoundMmio().write(addr, val);
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
