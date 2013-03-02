/*
  IMemoryDecoder.java

  (c) 2012-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.common;

import java.util.BitSet;

import org.eclipse.jface.viewers.ILabelProvider;

import v9t9.gui.client.swt.shells.debugger.IDecodedContent;

/**
 * This allows portions of memory to be decoded into structured units
 * (e.g. disassembly, graphics, etc).
 * @author ejs
 *
 */
public interface IMemoryDecoder {
	void reset();
	/**
	 * Initialize the decoder for the given range
	 * @param range
	 */
	void addRange(int addr, int size);
	/**
	 * Update the decoder for the given range
	 * @param addrSet
	 */
	void updateRange(BitSet addrSet);
	
	/**
	 * Return number of items to display from the initialized range
	 * @param addr 
	 * @param size 
	 * @return
	 */
	int getItemCount(int addr, int size);
	
	int getFirstItemIndex(int addr);
	/**
	 * Get the label provider for {@link IDecodedContent#getContent()} elements
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
}
