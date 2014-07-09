/*
  F99bConsoleMmioArea.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.TIMemoryModel;
import v9t9.machine.f99b.machine.InternalCruF99;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;

/**
 * Forth9900 memory-mapped I/O area
 * @author ejs
 *
 */
public class Forth9900ConsoleMmioArea extends ConsoleMmioArea  {
	
	public static final int VDPRD = 0x40;
	public static final int VDPST = 0x42;
	public static final int VDPWD = 0x44;
	public static final int VDPWA = 0x46;
	public static final int VDPWAL = 0x47;
	public static final int VDPCL = 0x48;
	public static final int VDPWI = 0x4A;
	private static final int[] forth9900ToVdpPort = { 0, 1, 2, 3, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
	
	
	public static final int GPLRD = 0x50;
	public static final int GPLRA = 0x52;
	public static final int GPLWD = 0x54;
	public static final int GPLWA = 0x56;
	
	public static final int SPCHWT = 0x58;
	public static final int SPCHRD = 0x5A;
	
	// character outlet
	public static final int DBG = 0x5E;
	
	public static final int SOUND = 0x60;	// 0x20!
	
	public static final int CRU_BASE = 0x80;
	public static final int CRU_END = 0x100;
	
	public static final int COLD = 0x114;
	
	private final IMachine machine;
		
	public Forth9900ConsoleMmioArea(IMachine machine) {
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
	    		getTIMemoryModel().getVdpMmio().write(forth9900ToVdpPort[addr], val);
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
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryArea#clearMemoryOnLoad(v9t9.common.memory.IMemoryEntry)
	 */
	@Override
	protected void clearMemoryOnLoad(IMemoryEntry memoryEntry) {
		int end = memoryEntry.getAddr() + memoryEntry.getSize();
		for (int addr = memoryEntry.getAddr(); addr < end; addr++) {
			if (addr >= 0x100) {
				memoryEntry.flatWriteByte(addr, (byte) 0);
			}
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryArea#loadChunk(v9t9.common.memory.IMemoryEntry, int, byte[])
	 */
	@Override
	protected void loadChunk(IMemoryEntry memoryEntry, int saveAddr,
			byte[] chunk) {
		if (saveAddr >= 0x100) {
			super.loadChunk(memoryEntry, saveAddr, chunk);
		} else {
			for (int addr = saveAddr; addr < saveAddr + chunk.length; addr++) {
				if (addr >= 0x100) {
					memoryEntry.flatWriteByte(addr, chunk[addr - saveAddr]);
				}
			}
		}
	}
}
