/**
 * 
 */
package v9t9.gui.common;

import v9t9.common.memory.IMemoryEntry;

/**
 * This allows portions of memory to be decoded into structured units
 * (e.g. disassembly, graphics, etc).
 * @author ejs
 *
 */
public interface IMemoryDecoderProvider {
	/**
	 * Get the decoder for this memory entry (or <code>null</code>)
	 */
	IMemoryDecoder getMemoryDecoder(IMemoryEntry entry);
	

}
