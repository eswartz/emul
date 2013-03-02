/*
  ResolveException.java

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
package v9t9.common.asm;


import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class ResolveException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3289343375936751890L;
	private final IOperand op;
	private String string;

	public ResolveException(IOperand op) {
		this.op = op;
		this.string = "Unresolved operand";
	}
	public ResolveException(IOperand op, String string) {
		this.op = op;
		this.string = string;
	}

	public ResolveException(IInstruction inst, IOperand op1,
			String string) {
		this.op = op1;
		this.string = string + ": >" + HexUtils.toHex4(inst.getPc()) + "=" + inst.toString();
	}
	@Override
	public String toString() {
		return string + ": " + op;
	}
	
	@Override
	public String getMessage() {
		return toString();
	}
}
