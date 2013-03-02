/*
  RegisterEventFormatter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.IOException;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoOutputEventBuffer;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.events.WriteRegisterEvent;

/**
 * @author ejs
 *
 */
public abstract class RegisterEventFormatter extends BaseEventFormatter  {
	
	public RegisterEventFormatter(String bufferId, String eventId) {
		super(bufferId, eventId);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		int reg = ((DemoInputEventBuffer) buffer).readVar(); 
		int val = ((DemoInputEventBuffer) buffer).readVar(); 
		//System.err.println("reg: " + Integer.toHexString(regOrAddr) +" = " + regVal);
		return createRegisterEvent(reg, val);
	}

	protected abstract VideoWriteRegisterEvent createRegisterEvent(int reg, int val);

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		WriteRegisterEvent ev = (WriteRegisterEvent) event;
		((DemoOutputEventBuffer) buffer).pushVar(ev.getReg());
		((DemoOutputEventBuffer) buffer).pushVar(ev.getVal());
	}

}
