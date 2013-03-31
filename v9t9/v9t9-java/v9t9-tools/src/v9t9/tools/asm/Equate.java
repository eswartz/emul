/*
  Equate.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;

/**
 * This is a symbol equated to a constant
 * @author ejs
 *
 */
public class Equate extends Symbol {

	private AssemblerOperand value;

	public Equate(SymbolTable table, String name, AssemblerOperand operand, int addr) {
		super(table, name);
		this.value = operand;
		setAddr(addr);
	}

	public Equate(SymbolTable table, String name, int addr) {
		super(table, name);
		this.value = new NumberOperand(addr);
		setAddr(addr);
	}
	public AssemblerOperand getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Equate other = (Equate) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	
}
