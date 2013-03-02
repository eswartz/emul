/*
  IDemoOutputBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * This interface represents a buffered amount of demo data that
 * is encoded per timer tick.
 * @author ejs
 *
 */
public interface IDemoOutputBuffer {
	boolean isEmpty();
	void flush() throws IOException;
	
	void push(byte val) throws IOException;
	
	void pushData(byte[] chunk, int offs, int len) throws IOException;
	void pushData(byte[] data) throws IOException;
}
