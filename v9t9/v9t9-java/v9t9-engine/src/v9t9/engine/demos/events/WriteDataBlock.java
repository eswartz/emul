/*
  WriteDataBlock.java

  (c) 2012 Edward Swartz

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
 * Write data in successive addresses
 * @author ejs
 *
 */
public abstract class WriteDataBlock implements IDemoEvent {

	protected int address;
	protected byte[] data;
	protected final int offs;
	protected final int length;
	
	public WriteDataBlock(int address, byte[] data, int offs, int length) {
		this.address = address;
		this.data = data;
		this.offs = offs;
		this.length = length;
	}
	public int getAddress() {
		return address;
	}
	public byte[] getData() {
		return data;
	}
	public int getLength() {
		return length;
	}
	public int getOffset() {
		return offs;
	}
	protected abstract IMemoryDomain getDomain(IMachine machine);
	
}