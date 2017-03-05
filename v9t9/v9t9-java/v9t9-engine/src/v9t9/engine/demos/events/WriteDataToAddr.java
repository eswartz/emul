/*
  WriteDataToAddr.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * Write data all to the same address
 * @author ejs
 *
 */
public abstract class WriteDataToAddr implements IDemoEvent {
	private int address;
	private byte[] data;
	private int length;
	
	public WriteDataToAddr(int address, byte[] data, int length) {
		this.address = address;
		this.data = data;
		this.length = length;
	}
	public int getAddress() {
		return address;
	}
	public byte[] getData() {
		return data;
	}
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	

	protected abstract IMemoryDomain getDomain(IMachine machine);
	
}