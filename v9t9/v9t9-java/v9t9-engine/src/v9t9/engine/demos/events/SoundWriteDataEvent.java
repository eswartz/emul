/*
  SoundWriteDataEvent.java

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
