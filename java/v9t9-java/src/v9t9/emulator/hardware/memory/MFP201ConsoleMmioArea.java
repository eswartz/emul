/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.engine.memory.*;

/**
 * MFP201 memory-mapped I/O area
 * 
 * Bytes from 0x0 to 0xFF are reserved for 8-bit peripherals.
 * Bytes from 0x100 to 0x1FF are reserved for 16-bit peripherals.
 * 
 * 
 * <pre>
        >80=VDPRD
        >81=VDPST
        >82=VDPWD
        >83=VDPWA
        >84=VDPCL
        >85=VDPWI
        
        >A0->BF=sound
        
</pre> 
 * @author ejs
 *
 */
public class MFP201ConsoleMmioArea extends ConsoleMmioArea implements MemoryListener {

	private static final int MMIO_BASE = 0x0;
	public static final int VDPRD = 0x80;
	public static final int VDPST = 0x81;
	public static final int VDPWD = 0x82;
	public static final int VDPWA = 0x83;
	public static final int VDPCL = 0x84;
	public static final int VDPWI = 0x85;
	public static final int SOUND = 0xA0;	// 0x20!
	private static final int RAMBASE = 0x200;
	
	private final Machine machine;
	private MemoryEntry underlyingMemory;
		
	MFP201ConsoleMmioArea(Machine machine) {
		super(0);
		this.machine = machine;
		machine.getMemory().addListener(this);
    };
    
    @Override
	public boolean hasWriteAccess() {
		return true;
	}

    @Override
	public boolean hasReadAccess() {
		return true;
	}


    
    public void physicalMemoryMapChanged(MemoryEntry entry) {
		if (entry.addr < MemoryDomain.AREASIZE) {
			findUnderlyingMemory();
		}
	}
    public void logicalMemoryMapChanged(MemoryEntry entry) {
    	physicalMemoryMapChanged(entry);
    }

    private void findUnderlyingMemory() {
    	for (MemoryEntry entry : machine.getMemory().getDomain("CPU").getMemoryEntries()) {
    		if (entry.addr < MMIO_BASE && entry.addr + entry.size >= 0x200 && entry.getArea() != this) {
    			underlyingMemory = entry;
    		}
    	}
	}

    @Override
    public void writeByte(MemoryEntry entry, int addr, byte val) {
    	if (isRAMAddr(addr)) {
    		underlyingMemory.getArea().flatWriteByte(underlyingMemory, addr, val);
    		return;
    	}
    	writeMmio(addr, val);
    }

	private boolean isRAMAddr(int addr) {
		return addr >= RAMBASE;
	}
    
	@Override
    public void writeWord(MemoryEntry entry, int addr, short val) {
    	if (isRAMAddr(addr)) {
    		underlyingMemory.getArea().flatWriteWord(underlyingMemory, addr, val);
    		return;
    	}
    	if (addr < 0x100)
    		writeByte(entry, addr, (byte) (val >> 8));
    	else 
    		writeWord(entry, addr, val);
    }


	@Override
	public byte readByte(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return underlyingMemory.getArea().flatReadByte(underlyingMemory, addr);
		return readMmio(addr);
	}
	
	@Override
	public short readWord(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return underlyingMemory.getArea().flatReadWord(underlyingMemory, addr);
		short word = readByte(entry, addr);
		if (addr < 0x100)
			word <<= 8;
		else
			word = (short) (((word & 0xff) << 8) | ((readByte(entry, addr + 1) & 0xff)));
		return (short) word;
	}

    private void writeMmio(int addr, byte val) {
    	switch (addr) {
    	case VDPWD:
    	case VDPWA:
    	case VDPCL:
    	case VDPWI:
    		getMFP201MemoryModel().getVdpMmio().write(addr, val);
    		break;
    	}

    	if (addr >= SOUND && addr < SOUND + 0x20) {
    		getMFP201MemoryModel().getSoundMmio().write(addr, val);
    	}
	}

	private MFP201MemoryModel getMFP201MemoryModel() {
		return (MFP201MemoryModel) machine.getMemoryModel();
	}

	private byte readMmio(int addr) {
		switch (addr) {
    	case VDPRD:
    	case VDPST:
    		return getMFP201MemoryModel().getVdpMmio().read(addr);
    	}
		return 0;
	}

	@Override
	public byte flatReadByte(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return super.flatReadByte(entry, addr);
		return 0;
	}
	
	@Override
	public short flatReadWord(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return super.flatReadWord(entry, addr);
		return 0;
	}
	
	@Override
	public void flatWriteByte(MemoryEntry entry, int addr, byte val) {
		if (isRAMAddr(addr))
			super.flatWriteByte(entry, addr, val);
	}

	@Override
	public void flatWriteWord(MemoryEntry entry, int addr, short val) {
		if (isRAMAddr(addr))
			super.flatWriteWord(entry, addr, val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.WordMemoryArea#getSize()
	 */
	@Override
	protected int getSize() {
		return 0x400;
	}
}
