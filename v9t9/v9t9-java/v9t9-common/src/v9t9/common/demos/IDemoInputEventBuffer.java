/*
  IDemoInputEventBuffer.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.IOException;
import java.util.Queue;

/**
 * @author ejs
 *
 */
public interface IDemoInputEventBuffer extends IDemoInputBuffer {

	/**
	 * Get the unique code for the buffer
	 * @return
	 */
	int getCode();
	
	/**
	 * Get the unique identifier for the buffer
	 * @return
	 */
	String getIdentifier();
	
	/**
	 * Decode events from the buffer
	 * @param queuedEvents
	 */
	void decodeEvents(Queue<IDemoEvent> queuedEvents) throws IOException;

	/**
	 * @param string
	 * @return
	 */
	IOException newBufferException(String string);

}
