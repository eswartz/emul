/**
 * 
 */
package v9t9.forthcomp;

import org.ejs.coffee.core.utils.HexUtils;

public class RelocEntry {
	public enum RelocType {
		RELOC_ABS_ADDR_16,
		RELOC_CALL_15S1, 
		RELOC_FORWARD,
	}
	public RelocEntry(int addr, RelocType type, int target) {
		this.addr = addr;
		this.type = type;
		this.target = target;
	}
	
	public String toString() {
		return type + ": " +  HexUtils.toHex4(addr) + " => " + HexUtils.toHex4(target);
	}
	
	public RelocType type;
	public int addr;
	public int target;
}

