/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryListener;
import v9t9.engine.memory.MultiBankedMemoryEntry;

/**
 * Enhanced memory-mapped I/O area, which is much compacted (and yes, sheds any 
 * potential for expansion of the GPL area... like anyone needs that...)
 * <pre>
        >FF80=VDPRD
        >FF82=VDPST
        >FF84=VDPWD
        >FF86=VDPWA
        >FF88=VDPCL
        >FF8A=VDPWI
        
        >FF90=GPLRD
        >FF92=GPLRA
        >FF94=GPLWD
        >FF96=GPLWA
        
        >FFA0=sound
        
        >FFB0=speech
        
	    >FFC0=CPU ROM select               
	    >FFC2=FORTH ROM select             
	    >FFC4=>8400->9FFF is RAM            TODO
	    >FFC6=>8400->9FFF is MMIO (old-style)   TODO
	    
	    >FFFC=NMI interrupt vector          TODO
        
</pre> 
 * @author ejs
 *
 */
public class EnhanchedConsoleMmioArea extends ConsoleMmioArea implements MemoryListener {

	private static final int MMIO_BASE = 0xFF80;
	public static final int VDPRD = 0xFF80;
	public static final int VDPST = 0xFF82;
	public static final int VDPWD = 0xFF88;
	public static final int VDPWA = 0xFF8A;
	public static final int VDPCL = 0xFF8C;
	public static final int VDPWI = 0xFF8E;
	public static final int GPLRD = 0xFF90;
	public static final int GPLRA = 0xFF92;
	public static final int GPLWD = 0xFF94;
	public static final int GPLWA = 0xFF96;
	public static final int SOUND = 0xFFA0;
	public static final int SPCHWT = 0xFFB0;
	public static final int SPCHRD = 0xFFB2;
	public static final int BANKA = 0xFFC0;
	public static final int BANKB = 0xFFC2;
	public static final int NMI = 0xFFFC;
	
	private final Machine machine;
	private MemoryEntry underlyingMemory;
	private MultiBankedMemoryEntry romMemory;
		
	EnhanchedConsoleMmioArea(Machine machine) {
		super(0);
		this.machine = machine;
		machine.getMemory().addListener(this);
    };
    
    public void notifyMemoryMapChanged(MemoryEntry entry) {
		if ((entry.addr >= 0x10000 - MemoryDomain.AREASIZE || entry.addr + entry.size < 0x10000)
				|| (entry.addr < 0x4000)) {
			findUnderlyingMemory();
		}
	}

    private void findUnderlyingMemory() {
    	for (MemoryEntry entry : machine.getMemory().getDomain("CPU").getMemoryEntries()) {
    		if (entry.addr < MMIO_BASE && entry.addr + entry.size >= 0x10000 && entry.area != this) {
    			underlyingMemory = entry;
    		}
    		else if (entry.addr < 0x4000) {
    			if (entry instanceof MultiBankedMemoryEntry) {
					romMemory = (MultiBankedMemoryEntry) entry;
				} else {
					romMemory = null;
				}
    		}

    	}
	}

    @Override
    public void writeByte(MemoryEntry entry, int addr, byte val) {
    	if (addr < MMIO_BASE || addr >= NMI) {
    		underlyingMemory.area.flatWriteByte(underlyingMemory, addr, val);
    		return;
    	}
    	if ((addr & 1) != 0)
    		return;
    	
    	writeMmio(addr, val);
    }
    
	@Override
    public void writeWord(MemoryEntry entry, int addr, short val) {
    	if (addr < MMIO_BASE || addr >= NMI) {
    		underlyingMemory.area.flatWriteWord(underlyingMemory, addr, val);
    		return;
    	}
    	writeByte(entry, addr, (byte) (val >> 8));
    }


	@Override
	public byte readByte(MemoryEntry entry, int addr) {
		if (addr < MMIO_BASE || addr >= NMI)
			return underlyingMemory.area.flatReadByte(underlyingMemory, addr);
    	if ((addr & 1) != 0)
    		return 0;
		return readMmio(addr);
	}
	
	@Override
	public short readWord(MemoryEntry entry, int addr) {
		if (addr < MMIO_BASE || addr >= NMI)
			return underlyingMemory.area.flatReadWord(underlyingMemory, addr);
		return (short) (readByte(entry, addr) << 8);
	}

    private void writeMmio(int addr, byte val) {
    	switch (addr) {
    	case VDPWD:
    	case VDPWA:
    	case VDPCL:
    	case VDPWI:
    		machine.getMemoryModel().getVdpMmio().write(addr, val);
    		break;
    	case GPLWA:
    		machine.getMemoryModel().getGplMmio().write(addr, val);
    		break;
    	case SOUND:
    		machine.getMemoryModel().getSoundMmio().write(addr, val);
    		break;
    	case SPCHWT:
    		machine.getMemoryModel().getSpeechMmio().write(addr, val);
    		break;
    	case BANKA:
    		if (romMemory != null) {
    			romMemory.selectBank(0);
    		}
    		break;
    	case BANKB:
    		if (romMemory != null) {
    			romMemory.selectBank(1);
    		}
    		break;
    	}
		
	}

	private byte readMmio(int addr) {
		switch (addr) {
    	case VDPRD:
    	case VDPST:
    		return machine.getMemoryModel().getVdpMmio().read(addr);
    	case GPLRD:
    	case GPLRA:
    		return machine.getMemoryModel().getGplMmio().read(addr);
    	case SPCHRD:
    		return machine.getMemoryModel().getSpeechMmio().read(addr);
    	}
		return 0;
	}


}
