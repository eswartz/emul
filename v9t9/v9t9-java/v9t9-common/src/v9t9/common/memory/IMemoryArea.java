/**
 * 
 */
package v9t9.common.memory;


/**
 * @author ejs
 *
 */
public interface IMemoryArea extends Comparable<IMemoryArea> {

	boolean hasWriteAccess();

	boolean hasReadAccess();

	/**
	 * Read a word at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	short flatReadWord(IMemoryEntry entry, int addr);

	/**
	 * Read a byte at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	byte flatReadByte(IMemoryEntry entry, int addr);

	/**
	 * Write a word at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	void flatWriteWord(IMemoryEntry entry, int addr, short val);

	/**
	 * Write a byte at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	void flatWriteByte(IMemoryEntry entry, int addr, byte val);

	/**
	 * Save the content of the memory the given array (sized based on the entry)
	 * @param array
	 */
	void copyToBytes(byte[] array);

	/**
	 * Read the content of the memory the given array (sized based on the entry)
	 * @param array
	 */
	void copyFromBytes(byte[] array);

	void setLatency(int latency);

}