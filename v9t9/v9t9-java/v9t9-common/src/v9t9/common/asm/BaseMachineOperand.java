/*
  BaseMachineOperand.java

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
package v9t9.common.asm;


/**
 * @author Ed
 *
 */
public abstract class BaseMachineOperand implements IMachineOperand {

	public int type = OP_NONE;
	/** value in opcode, usually register or count */
	public int val = 0;
	/** immediate word */
	public short immed = 0;
	public int dest = OP_DEST_FALSE;
	public boolean bIsReference = false;
}