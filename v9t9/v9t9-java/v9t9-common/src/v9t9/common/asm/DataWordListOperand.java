/*
  DataWordListOperand.java

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
public class DataWordListOperand implements IOperand {

	private int[] args;

	public DataWordListOperand(int[] args) {
		this.args = args;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("args = { ");
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append('>');
			builder.append(HexUtils.toHex4(args[i]));
		}
		builder.append(" }");
		return builder.toString();
	}
}
