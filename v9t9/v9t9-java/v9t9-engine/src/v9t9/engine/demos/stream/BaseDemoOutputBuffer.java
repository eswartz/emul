/*
  BaseDemoOutputBuffer.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputBuffer;


/**
 * @author ejs
 * 
 */
public abstract class BaseDemoOutputBuffer implements IDemoOutputBuffer {

	protected final OutputStream stream;

	public abstract boolean isAvailable(int i);

	/**
	 * @param stream
	 * 
	 */
	public BaseDemoOutputBuffer(OutputStream stream) {
		this.stream = stream;
	}

	public void pushData(byte[] data) throws IOException {
		pushData(data, 0, data.length);
	}

}