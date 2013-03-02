/*
  SimpleBreakpoint.java

  (c) 2012 Edward Swartz

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
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public class SimpleBreakpoint implements IBreakpoint {

	private final int pc;
	private final boolean oneShot;

	public SimpleBreakpoint(int pc, boolean oneShot) {
		this.pc = pc;
		this.oneShot = oneShot;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#getPc()
	 */
	@Override
	public int getPc() {
		return pc;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#execute(v9t9.common.cpu.ICpuState)
	 */
	@Override
	public boolean execute(ICpuState cpu) {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		return oneShot;
	}

}
