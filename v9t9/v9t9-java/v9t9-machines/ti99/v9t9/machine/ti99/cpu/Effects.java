/*
  Effects.java

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
package v9t9.machine.ti99.cpu;

public class Effects {
	/** method status is set after operands parsed, before execution (Status.stset_xxx) */
    public int stsetBefore; // 
    /** method status is set after execution (Status.stset_xxx) */
    public int stsetAfter; // 
    /** bits read by instruction (Status.ST_xxx mask) */
    public int stReads;     // 
    /** operand is a jump (INST_JUMP_COND = conditional) */
    public int jump; 

    /** what resources (INST_RSRC_xxx) are read and written? */
    public int reads, writes;	
    public int mop1_dest, mop2_dest, mop3_dest;
    
    public boolean byteop;
}