/*
  RelocEntry.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import ejs.base.utils.HexUtils;

public class RelocEntry {
	public enum RelocType {
		RELOC_ABS_ADDR_16,
		RELOC_CALL_15S1, 
		RELOC_CONSTANT,
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

