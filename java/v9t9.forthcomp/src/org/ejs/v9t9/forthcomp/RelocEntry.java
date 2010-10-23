/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import org.ejs.coffee.core.utils.HexUtils;

class RelocEntry {
	public enum RelocType {
		RELOC_ABS_ADDR_16,
		RELOC_CALL_15S1,
	}
	public RelocEntry(int addr, RelocType type, int target) {
		this.addr = addr;
		this.type = type;
		this.target = target;
	}
	
	public String toString() {
		return type + ": " + addr + " => " + HexUtils.toHex4(target);
	}
	
	RelocType type;
	int addr;
	int target;
}

