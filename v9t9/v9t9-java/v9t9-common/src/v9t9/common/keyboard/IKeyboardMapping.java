/*
  IKeyboardMapping.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;



/**
 * Provides a dynamic interpretation of the keyboard mapping
 * for the machine
 * @author ejs
 *
 */
public interface IKeyboardMapping {
	/**
	 * Representation of a key on a physical keyboard,
	 * mapping an id to a rectangle.  For example, if units are in 
	 * key halves, then a normal key is 2x2, while e.g.
	 * Tab is 3x2, Shift and Enter are 4x2, and Space
	 * is 12x2.  
	 * @author ejs
	 *
	 */
	class PhysKey {
		public final Object keyId;
		public final int x, y;
		public final int width, height;
		public PhysKey(Object keyId, int x, int y, int width, int height) {
			this.keyId = keyId;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.valueOf(keyId);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + width;
			result = prime * result + height;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PhysKey other = (PhysKey) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			if (width != other.width)
				return false;
			if (height != other.height)
				return false;
			return true;
		}
		
		
	}
	
	/**
	 * Get the physical keys on the emulated keyboard
	 * @return non-<code>null</code> array
	 */
	PhysKey[] getPhysicalLayout();
	
	/**
	 * Get the modes for the keyboard.
	 * @return non-<code>null</code> array
	 */
	IKeyboardMode[] getModes();
	
	/**
	 * Find a mode
	 * @param id mode id
	 * @return mode  or <code>null</code>
	 */
	IKeyboardMode getMode(String id);
	
}
