/*
  IPIOHandler.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;




/**
 * This interface provides the backend for an emulated parallel port.
 * @author ejs
 *
 */
public interface IPIOHandler {

	void addListener(IPIOListener listener);
	void removeListener(IPIOListener listener);

	/**
	 * Transmit characters from the transmit buffer 
	 */
	void transmitChars(IOBuffer buf);

}

