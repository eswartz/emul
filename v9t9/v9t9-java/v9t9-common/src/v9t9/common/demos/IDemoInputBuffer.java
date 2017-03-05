/*
  IDemoInputBuffer.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * This interface represents a buffered amount of demo data that
 * is decoded per timer tick.
 * @author ejs
 *
 */
public interface IDemoInputBuffer {
	/**
	 * Refill the buffer.
	 * @throws IOException
	 */
	void refill() throws IOException;

	/**
	 * Get position in enclosing stream, for the purpose of reporting errors
	 * @return
	 */
	long getEffectivePos();
	
	/**
	 * Tell if content is available in the buffer
	 * @return
	 */
	boolean isAvailable();

	/**
	 * Read a byte
	 * @return
	 * @throws IOException 
	 */
	int read() throws IOException;
	
	byte[] readData(int chunkLength) throws IOException;
	
	byte[] readRest() throws IOException;
}
