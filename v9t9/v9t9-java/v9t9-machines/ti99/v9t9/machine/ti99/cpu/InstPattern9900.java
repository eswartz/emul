/*
  InstPattern9900.java

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
package v9t9.machine.ti99.cpu;

public class InstPattern9900 {
	public final int op1;
	public final int op2;
	final int off1;
	public final static int NONE = 0;
	public final static int REG = 1;
	public final static int CNT = 2;
	public final static int IMM = 3;
	public final static int GEN = 4;
	public final static int OFF = 5;

	InstPattern9900(int op1, int op2, int off1) {
		this.op1 = op1;
		this.op2 = op2;
		this.off1 = off1;
	}
	InstPattern9900(int op1, int op2) {
		this(op1, op2, 0);
	}
}