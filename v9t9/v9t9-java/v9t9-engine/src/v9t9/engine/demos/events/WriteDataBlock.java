/*
  WriteDataBlock.java

  (c) 2012 Edward Swartz

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