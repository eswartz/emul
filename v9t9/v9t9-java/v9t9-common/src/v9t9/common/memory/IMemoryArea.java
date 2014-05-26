/*
  IMemoryArea.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;


/**
 * @author ejs
 *
 */
public interface IMemoryArea extends Comparable<IMemoryArea>, IMemoryAccess {

	boolean hasWriteAccess();

	boolean hasReadAccess();

	/**
	 * Get the offset of the area in its containing entry.
	 * @return
	 */
	int getOffset();
	
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

	int getLatency();
	void setLatency(int latency);

}