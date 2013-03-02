/*
  IDemoInputBuffer.java

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
