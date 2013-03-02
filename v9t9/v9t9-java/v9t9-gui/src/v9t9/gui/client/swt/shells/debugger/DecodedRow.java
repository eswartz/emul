/*
  DecodedRow.java

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
package v9t9.gui.client.swt.shells.debugger;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class DecodedRow {

	private final IDecodedContent content;
	private final byte[] bytes;

	/**
	 * @param addr
	 * @param range
	 * @param decode
	 */
	public DecodedRow(IDecodedContent content, byte[] bytes) {
		this.bytes = bytes;
		this.content = content;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
		result = prime * result + content.getAddr();
		result = prime * result + content.getSize();
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecodedRow other = (DecodedRow) obj;
		if (bytes == null) {
			if (other.bytes != null)
				return false;
		} else if (!Arrays.equals(bytes, other.bytes))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else {
			if (content.getAddr() != other.content.getAddr())
				return false;
			if (content.getSize() != other.content.getSize())
				return false;
		}
		return true;
	}



	/**
	 * @return the content
	 */
	public IDecodedContent getContent() {
		return content;
	}

	/**
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}
	

}
