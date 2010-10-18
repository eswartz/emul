/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.engine.memory.*;

/**
 * F99 memory-mapped I/O area
 * <pre>
        >FFA0=sound
        
        >FFB0=speech
        
	    >FFC0=int vecs (32 * word)
	    
        >FFE0=VDPRD
        >FFE2=VDPST
        >FFE4=VDPWD
        >FFE6=VDPWA
        >FFE8=VDPCL
        >FFEA=VDPWI
        
        >FFF0=GPLRD
        >FFF2=GPLRA
        >FFF4=GPLWD
        >FFF6=GPLWA

</pre> 
 * @author ejs
 *
 */
public class F99ConsoleMmioArea extends ConsoleMmioArea implements MemoryListener {

	private static final int MMIO_BASE = 0xFFA0;
	private static final int INT_BASE = 0xFFC0;
	private static final int INT_END = 0xFFE0;
	
	public static final int VDPRD = 0xFFE0;
	public static final int VDPST = 0xFFE2;
	public static final int VDPWD = 0xFFE8;
	public static final int VDPWA = 0xFFEA;
	public static final int VDPCL = 0xFFEC;
	public static final int VDPWI = 0xFFEE;
	public static final int GPLRD = 0xFFF0;
	public static final int GPLRA = 0xFFF2;
	public static final int GPLWD = 0xFFF4;
	public static final int GPLWA = 0xFFF6;
	public static final int SPCHWT = 0xFFF8;
	public static final int SPCHRD = 0xFFFA;
	public static final int SOUND = 0xFFA0;	// 0x20!
	
	private final Machine machine;
	private MemoryEntry underlyingMemory;
		
	F99ConsoleMmioArea(Machine machine) {
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
		if ((entry.addr >= 0x10000 - MemoryDomain.AREASIZE || entry.addr + entry.size < 0x10000)
				|| (entry.addr < 0x4000)) {
			findUnderlyingMemory();
		}
	}
    public void logicalMemoryMapChanged(MemoryEntry entry) {
    	physicalMemoryMapChanged(entry);
    }

    private void findUnderlyingMemory() {
    	for (MemoryEntry entry : machine.getMemory().getDomain("CPU").getMemoryEntries()) {
    		if (entry.addr + entry.size >= 0x10000 && entry.getArea() != this) {
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
    	if ((addr & 1) != 0)
    		return;
    	
    	writeMmio(addr, val);
    }

	private boolean isRAMAddr(int addr) {
		return addr < MMIO_BASE || (addr >= INT_BASE && addr < INT_END);
	}
    
	@Override
    public void writeWord(MemoryEntry entry, int addr, short val) {
    	if (isRAMAddr(addr)) {
    		underlyingMemory.getArea().flatWriteWord(underlyingMemory, addr, val);
    		return;
    	}
    	writeByte(entry, addr, (byte) (val >> 8));
    }


	@Override
	public byte readByte(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return underlyingMemory.getArea().flatReadByte(underlyingMemory, addr);
    	if ((addr & 1) != 0)
    		return 0;
		return readMmio(addr);
	}
	
	@Override
	public short readWord(MemoryEntry entry, int addr) {
		if (isRAMAddr(addr))
			return underlyingMemory.getArea().flatReadWord(underlyingMemory, addr);
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
