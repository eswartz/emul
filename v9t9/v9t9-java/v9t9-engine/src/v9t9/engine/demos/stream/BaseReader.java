/*
  BaseReader.java

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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ejs.base.utils.CountingInputStream;

/**
 * @author ejs
 *
 */
public class BaseReader {

	protected CountingInputStream is;

	/**
	 * 
	 */
	public BaseReader(InputStream is) {
		BufferedInputStream bis = is instanceof BufferedInputStream ? 
				(BufferedInputStream) is : new BufferedInputStream(is);
		this.is = new CountingInputStream(bis);
	}
	
	public long getPosition() {
		return is.getPosition();
	}

	public IOException newFormatException(String string) {
		return newFormatException(string, getPosition());
	}

	public IOException newFormatException(String string, long effectivePos) {
		return new IOException("Demo corrupted at 0x" + 
				Long.toHexString(effectivePos) + ": " + string);

	}

	/**
	 * @return the is
	 */
	public CountingInputStream getInputStream() {
		return is;
	}


}