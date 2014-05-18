/*
  VideoWriteDataEvent.java

  (c) 2012 Edward Swartz

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
public class PrinterImageEvent implements IDemoEvent {

	public static final String ID = "PrinterImage";
	
	public static final byte DATA = 0;
	public static final byte NEW_PAGE = 1;
	
	private byte[] data;

	private byte type;
	
	public static PrinterImageEvent writeData(byte[] data) {
		return new PrinterImageEvent(DATA, data);
	}
	public static PrinterImageEvent newPage() {
		return new PrinterImageEvent(NEW_PAGE, null);
	}
	private PrinterImageEvent(byte type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	public byte getType() {
		return type;
	}
	
	public byte[] getData() {
		return data;
	}
}
