/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * @author ejs
 *
 */
public interface MemoryTransfer {

	/**
	 * Read a parameter word
	 * @param i
	 * @return
	 */
	short readParamWord(int offset);
	/**
	 * Read a parameter byte
	 * @param i
	 * @return
	 */
	byte readParamByte(int offset);
	
	/**
	 * @param i
	 * @param err
	 */
	void writeParamByte(int offset, byte val);
	void writeParamWord(int offset, short val);
	/**
	 * Record a write to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @param read
	 */
	void dirtyVdpMemory(int vaddr, int read);

	/**
	 * Get memory read/write access to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return access to memory (need {@link #dirtyVdpMemory(short, int)} to notice)
	 */
	ByteMemoryAccess getVdpMemory(int vaddr);
	
	/**
	 * Read byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	byte readVdpByte(int vaddr);
	
	/**
	 * Read word in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	short readVdpShort(int vaddr);
	
	/**
	 * Write byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	void writeVdpByte(int vaddr, byte byt);

}
