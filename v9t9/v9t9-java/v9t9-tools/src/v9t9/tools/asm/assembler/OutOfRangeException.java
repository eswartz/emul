/*
  OutOfRangeException.java

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
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;

/**
 * @author ejs
 *
 */
public class OutOfRangeException extends ResolveException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7132296027636428230L;
	private final Symbol symbol;

	/**
	 * @param inst
	 * @param op
	 * @param string
	 */
	public OutOfRangeException(IInstruction inst, IOperand op, Symbol symbol, int range) {
		super(op, "Jump out of range (>" + HexUtils.toHex4((range / 2)) + " words)");
		this.symbol = symbol;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}

}
