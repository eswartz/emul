/**
 * 
 */
package v9t9.gui.common;

import org.eclipse.jface.viewers.ILabelProvider;

import v9t9.gui.client.swt.shells.debugger.IDecodedContent;
import v9t9.gui.client.swt.shells.debugger.MemoryRange;

/**
 * This allows portions of memory to be decoded into structured units
 * (e.g. disassembly, graphics, etc).
 * @author ejs
 *
 */
public interface IMemoryDecoder {

	/**
	 * Initialize the decoder for the given range
	 * @param range
	 */
	void initialize(MemoryRange range);
	
	/**
	 * Return number of items to display from the initialized range
	 * @return
	 */
	int getItemCount();

	
	
	/**
	 * Get the label provider for the content produced by {@link #decode(int, int)}.
	 * @return label provider
	 */
	ILabelProvider getLabelProvider();

	/**
	 * Get the basic chunk size in bytes of memory to decode at a time.
	 * @return
	 */
	int getChunkSize();

	/**
	 * Decode content from [addr, addr+K*chunkSize).
	 * @param addr
	 * @return content to render at addr.
	 */
	IDecodedContent decodeItem(int addr);

	/**
	 * Tell if the row at the given address is visible,
	 * given the previous visible address.
	 * @param addr
	 * @param previous
	 * @return true if addr's content is visible
	 */
	//boolean isAddrVisible(int addr, int previous);



}
