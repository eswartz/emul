/*
  SymbolOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class SymbolOperand extends BaseOperand {

	private final Symbol symbol;

	public SymbolOperand(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
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
		SymbolOperand other = (SymbolOperand) obj;
		if (symbol == null) {
			if (other.symbol != null) {
				return false;
			}
		} else if (!symbol.equals(other.symbol)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
	
	
	public LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException {
		if (symbol.isDefined()) {
			LLImmedOperand op = new LLImmedOperand(this, symbol.getAddr());
			return op;
		} else {
			return new LLForwardOperand(this, 2);
		}
	}

	public Symbol getSymbol() {
		return symbol;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new BinaryOperand('+', this, new NumberOperand(i));
	}
}
