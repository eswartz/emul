/*
  CircularByteIter.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.engine.files.image;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CircularByteIter implements Iterator<Byte> {

	private final byte[] buffer;
	private final int end;
	private int ptr, cnt;
	private int start;

	public CircularByteIter(byte[] buffer, int size) {
		this.buffer = buffer;
		this.end = size;
		this.ptr = 0;
		this.cnt = size;
	}
	public void setPointers(int start, int ptr) {
		this.start = start;
		this.ptr = ptr % end;
	}
	public int getPointer() {
		return ptr;
	}
	public boolean hasNext() {
		return cnt > 0;
	}

	public Byte next() {
		if (cnt <= 0)
			throw new NoSuchElementException();
		Byte ret = buffer[ptr];
		if (ptr + 1 >= end) {
			ptr = start;
		} else {
			ptr++;
		}
		cnt--;
		return ret;
	}
	
	public Byte peek() {
		if (cnt <= 0)
			throw new NoSuchElementException();
		Byte ret = buffer[ptr];
		return ret;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	public int remaining() {
		return cnt;
	}
	public void setCount(int i) {
		this.cnt = i;
	}
	public int getStart() {
		return start;
	}
	
}