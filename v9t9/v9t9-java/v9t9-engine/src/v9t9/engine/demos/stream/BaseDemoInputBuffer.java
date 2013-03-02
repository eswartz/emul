/*
  BaseDemoInputBuffer.java

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
import java.io.InputStream;

import v9t9.common.demos.IDemoInputBuffer;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoInputBuffer implements IDemoInputBuffer {

	protected final InputStream is;
	protected final String label;
	protected long startPos;

	/**
	 * 
	 */
	public BaseDemoInputBuffer(InputStream is, String label) {
		this.is = is;
		this.label = label;
	}


	protected IOException newFormatException(String string) {
		return newFormatException(string, startPos);
	}

	protected IOException newFormatException(String string, long effectivePos) {
		return new IOException("Demo corrupted at 0x" + 
				Long.toHexString(effectivePos) + ": " + string);

	}
	
	public IOException newBufferException(String string) {
		return newFormatException(string, getEffectivePos());
	}


}