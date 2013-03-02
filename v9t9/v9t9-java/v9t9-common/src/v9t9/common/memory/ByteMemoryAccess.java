/*
  ByteMemoryAccess.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;


/**
 * @author ejs
 *
 */
public class ByteMemoryAccess {
	public final byte[] memory;
	public int offset;

	public ByteMemoryAccess(byte[] memory, int offset) {
		this.memory = memory;
		this.offset = offset;
	}

	public ByteMemoryAccess(ByteMemoryAccess pattern) {
		this.memory = pattern.memory;
		this.offset = pattern.offset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + memory.hashCode();
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ByteMemoryAccess other = (ByteMemoryAccess) obj;
		if (memory != other.memory) {
			return false;
		}
		if (offset != other.offset) {
			return false;
		}
		return true;
	}
	
	
}
