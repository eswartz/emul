/**
 * 
 */
package v9t9.engine.dsr;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This interface encapsulates the kinds of memory access a DSR may perform.
 * It is abstracted out for purposes of unit testing.
 * @author ejs
 *
 */
public interface IMemoryTransfer {

	/**
	 * Read a parameter word
	 */
	short readParamWord(int offset);
	/**
	 * Read a parameter byte
	 */
	byte readParamByte(int offset);
	
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
