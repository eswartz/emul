/*
  Symbol.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.tools.asm.assembler;

import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class Symbol {

	private final String name;
	private int addr;
	private boolean defined;
	private int index;
	private final SymbolTable table;

	public Symbol(SymbolTable table, String name) {
		this.table = table;
		this.name = name;
	}

	public Symbol(SymbolTable table, String name, int addr) {
		this.table = table;
		this.name = name;
		this.addr = addr;
		this.defined = true;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + (isDefined() ? "{>"+HexUtils.toHex4(addr)+"}" : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result + ((name == null) ? 0 : name.hashCode()))
			* prime + (table == null ? 0 : table.hashCode());
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
		Symbol other = (Symbol) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (table == null) {
			if (other.table != null) {
				return false;
			}
		} else if (!table.equals(other.table)) {
			return false;
		}
		return true;
	}

	public void setAddr(int addr) {
		this.addr = addr;
		this.defined = true;
	}

	/** Is the real address known for this symbol? */
	public boolean isDefined() {
		return defined;
	}

	/** Is the real address known for this symbol? */
	public void setDefined(boolean b) {
		this.defined = b;
	}
	
	
	public int getAddr() {
		return addr & 0xffff;
	}


	public void setIndex(int i) {
		this.index = i;
	}
	
	public int getIndex() {
		return index;
	}
	
	public SymbolTable getTable() {
		return table;
	}

	
}
