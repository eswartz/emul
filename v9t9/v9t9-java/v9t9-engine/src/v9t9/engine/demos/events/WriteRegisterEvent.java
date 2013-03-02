/*
  WriteRegisterEvent.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

public abstract class WriteRegisterEvent implements IDemoEvent {
	private int reg;
	private int val;
	
	public WriteRegisterEvent(int reg, int val) {
		this.reg = reg;
		this.val = val;
	}
	public int getReg() {
		return reg;
	}
	public int getVal() {
		return val;
	}
}