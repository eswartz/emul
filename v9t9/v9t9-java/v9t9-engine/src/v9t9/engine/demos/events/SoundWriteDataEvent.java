/*
  SoundWriteDataEvent.java

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
 * @author ejs
 *
 */
public class SoundWriteDataEvent extends WriteDataToAddr implements IDemoEvent {

	public static final String ID = "SoundWriteData";

	public SoundWriteDataEvent(int address, byte[] data) {
		super(address, data, data.length);
	}

	public SoundWriteDataEvent(int addr, byte[] data,
			int length) {
		super(addr, data, length);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataToAddr#getDomain(v9t9.common.machine.IMachine)
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getConsole();
	}

}
