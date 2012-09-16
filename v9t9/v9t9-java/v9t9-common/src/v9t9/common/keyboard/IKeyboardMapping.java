/**
 * 
 */
package v9t9.common.keyboard;

import java.util.Map;

import v9t9.common.machine.IMachine;

/**
 * Provides a dynamic interpretation of the keyboard mapping
 * for the machine
 * @author ejs
 *
 */
public interface IKeyboardMapping {
	class PhysKey {
		public final String keyId;
		public final int x, y;
		public final int width, height;
		public PhysKey(String keyId, int x, int y, int width, int height) {
			this.keyId = keyId;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
	}
	
	class Key {
		public final String id;
		public final String label;

		public Key(String id, String label) {
			this.id = id;
			this.label = label;
		}
	}
	
	PhysKey[] getPhysicalLayout();
	String[] getModes();
	String getCurrentMode(IMachine machine);
	
	Map<String, String> getLogicalLayout(String mode);
}
