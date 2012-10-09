/**
 * 
 */
package v9t9.common.keyboard;

import java.util.Map;

import v9t9.common.machine.IMachine;

import ejs.base.utils.Pair;

/**
 * Provides a dynamic interpretation of the keyboard mapping
 * for the machine
 * @author ejs
 *
 */
public interface IKeyboardMapping {
	/**
	 * Representation of a key on a physical keyboard,
	 * mapping an id to a rectangle.  Units are in 
	 * key halves -- a normal key is 2x2, while e.g.
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
			result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
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
			if (keyId == null) {
				if (other.keyId != null)
					return false;
			} else if (!keyId.equals(other.keyId))
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
