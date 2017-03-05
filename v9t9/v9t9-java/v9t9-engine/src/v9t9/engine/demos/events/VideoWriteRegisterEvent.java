/*
  VideoWriteRegisterEvent.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

/**
 * @author ejs
 *
 */
public class VideoWriteRegisterEvent extends WriteRegisterEvent implements
		IDemoEvent {

	public static final String ID = "VideoWriteRegister";

	public VideoWriteRegisterEvent(int addr) {
		super((addr & 0x7f00) >> 8, (addr & 0xff));
	}

	public VideoWriteRegisterEvent(int reg, int val) {
		super(reg, val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}

	/**
	 * @return
	 */
	public int getAddr() {
		return (getReg() << 8) | 0x8000 | (getVal() & 0xff);
	}

}
