/*
  CircularByteIter.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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