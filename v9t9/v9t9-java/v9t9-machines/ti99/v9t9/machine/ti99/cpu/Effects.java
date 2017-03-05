/*
  Effects.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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