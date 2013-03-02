/*
  LabelDirective.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.directive;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.Symbol;

/**
 * @author Ed
 *
 */
public class LabelDirective extends Directive {

	private final Symbol symbol;

	public LabelDirective(Symbol symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String toString() {
		return symbol.getName() + ":";
	}
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		symbol.setAddr(assembler.getPc());
		setPc(assembler.getPc());

		return new IInstruction[] { this };
	}

	@Override
	public void setPc(int pc) {
		super.setPc(pc);
		symbol.setAddr(pc);
	}
	public Symbol getSymbol() {
		return symbol;
	}

}
