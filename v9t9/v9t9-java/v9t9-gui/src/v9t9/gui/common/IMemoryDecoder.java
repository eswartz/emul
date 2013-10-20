/*
  IMemoryDecoder.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.common;

import java.util.BitSet;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Menu;

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
	/**
	 * @param menu
	 * @param row
	 */
	void fillMenu(Menu menu, IDecodedContent content);
}
