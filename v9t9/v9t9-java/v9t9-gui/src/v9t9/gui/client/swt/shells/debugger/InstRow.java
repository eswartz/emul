/*
  InstRow.java

  (c) 2009-2012 Edward Swartz

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
package v9t9.gui.client.swt.shells.debugger;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.InstructionWorkBlock;

/**
 * @author ejs
 *
 */
public class InstRow {

	private static int gCounter;
	private final int count = gCounter++;
	private final InstructionWorkBlock before;
	private final InstructionWorkBlock after;
	private boolean isGeneric;
	
	public InstRow(InstructionWorkBlock before) {
		this.before = before;
		this.after = before;
		this.isGeneric = true;
	}
	public InstRow(InstructionWorkBlock before, InstructionWorkBlock after) {
		this.before = before;
		this.after = after;
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
		InstRow other = (InstRow) obj;
		if (count != other.count) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return count;
	}

	/**
	 * @return
	 */
	public boolean isGeneric() {
		return isGeneric;
	}
	/**
	 * @return
	 */
	public RawInstruction getBeforeInst() {
		return before.inst;
	}
	public RawInstruction getAfterInst() {
		return after.inst;
	}
	/**
	 * @return the after
	 */
	public InstructionWorkBlock getAfter() {
		return after;
	}
	/**
	 * @return the before
	 */
	public InstructionWorkBlock getBefore() {
		return before;
	}
}
