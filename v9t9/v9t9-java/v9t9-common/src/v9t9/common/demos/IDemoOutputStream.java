/*
  IDemoOutputStream.java

  (c) 2012-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ejs
 *
 */
public interface IDemoOutputStream extends Closeable {
	/** Get underlying stream */
	OutputStream getOutputStream();

	/**
	 * Get the format (magic number)
	 */
	byte[] getDemoFormat();

	/**
	 * Register a custom buffer and event type.
	 * @param buffer
	 * @param eventId
	 * @throws IOException
	 */
	void registerBuffer(IDemoOutputEventBuffer buffer, String eventId) throws IOException;
	
	/**
	 * Write new event.
	 * @return event
	 * @throws IOException 
	 */
	void writeEvent(IDemoEvent event) throws IOException;

	/**
	 * Get the rate at which timer events occur
	 * @return Hz
	 */
	int getTimerRate();

	/**
	 * Get the position of the stream in bytes
	 */
	long getPosition();
	
	/**
	 * Get the elapsed time of the demo (after the previous event) in ms.
	 */
	long getElapsedTime();
}
