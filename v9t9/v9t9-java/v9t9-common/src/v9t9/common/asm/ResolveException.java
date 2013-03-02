/*
  ResolveException.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
