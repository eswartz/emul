/*
  RelocEntry.java

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
package v9t9.tools.forthcomp;

import ejs.base.utils.HexUtils;

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

