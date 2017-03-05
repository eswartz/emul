/*
  IFetchStateTracker.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

/**
 * Track CPU state while instructions are being fetched
 * @author ejs
 *
 */
public interface IFetchStateTracker {
	IFetchStateTracker getParent();
	void setParent(IFetchStateTracker parent);
	
	int fetchRegister(int reg);
	short fetchWord(int addr);
	byte fetchByte(int addr);
}
