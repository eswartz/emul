/*
  IMemoryIOHandler.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr;

/**
 * @author ejs
 *
 */
public interface IMemoryIOHandler {

	void writeData(int addr, byte val);
	byte readData(int addr);
	
	boolean handlesAddress(int addr);
}
