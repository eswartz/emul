/*
  IMemoryDecoderProvider.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
