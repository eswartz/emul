/**
 * 
 */
package v9t9.engine.dsr;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.MemoryDomain;
import v9t9.engine.hardware.IVdpChip;

/**
 * @author ejs
 *
 */
public class ConsoleMemoryTransfer implements IMemoryTransfer {

	private final IVdpChip vdpHandler;
	private final MemoryDomain console;
	private final short rambase;

	public ConsoleMemoryTransfer(MemoryDomain console, IVdpChip vdpHandler, short rambase) {
		this.console = console;
		this.vdpHandler = vdpHandler;
		this.rambase = rambase;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#readParamWord(int)
	 */
	public short readParamWord(int offset) {
		return console.readWord(rambase + offset);
	}
	
	public byte readParamByte(int offset) {
		return console.readByte(rambase + offset);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#writeParamByte(int, byte)
	 */
	public void writeParamByte(int offset, byte val) {
		console.writeByte(rambase + offset, val);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#writeParamWord(int, short)
	 */
	public void writeParamWord(int offset, short val) {
		console.writeWord(rambase + offset, val);
		
	}
	/**
	 * Record a write to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @param read
	 */
	public void dirtyVdpMemory(int vaddr, int read) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		while (read-- > 0) {
			vdpHandler.touchAbsoluteVdpMemory(
					base + vaddr, 
					(byte) vdpHandler.readAbsoluteVdpMemory(base + vaddr));
			vaddr++;
		}
	}

	/**
	 * Get memory read/write access to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return access to memory (need {@link #dirtyVdpMemory(short, int)} to notice)
	 */
	public ByteMemoryAccess getVdpMemory(int vaddr) {
		return vdpHandler.getByteReadMemoryAccess(
				vdpHandler.getVdpMmio().getBankAddr() + vaddr);
	}
	
	/**
	 * Read byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public byte readVdpByte(int vaddr) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		return vdpHandler.readAbsoluteVdpMemory(base + vaddr);
	}
	
	/**
	 * Read word in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public short readVdpShort(int vaddr) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		return (short) ((vdpHandler.readAbsoluteVdpMemory(base + vaddr) << 8)
			| (vdpHandler.readAbsoluteVdpMemory(base + vaddr + 1) & 0xff));
	}
	
	/**
	 * Write byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public void writeVdpByte(int vaddr, byte byt) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		vdpHandler.writeAbsoluteVdpMemory(base + vaddr, byt);
	}

}