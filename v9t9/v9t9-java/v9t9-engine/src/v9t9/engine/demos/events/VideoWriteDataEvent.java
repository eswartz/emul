/*
  VideoWriteDataEvent.java

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
 * @author ejs
 *
 */
public class VideoWriteDataEvent extends WriteDataBlock implements IDemoEvent {

	public static final String ID = "VideoWriteData";

	public VideoWriteDataEvent(int address, byte[] data, int offs, int length) {
		super(address, data, offs, length);
	}

	public VideoWriteDataEvent(int address, byte[] data, int length) {
		super(address, data, 0, length);
	}
	
	public VideoWriteDataEvent(int address, byte[] data) {
		super(address, data, 0, data.length);
	}


	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataBlock#getDomain()
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getMemory().getDomain(IMemoryDomain.NAME_VIDEO);
	}
}
