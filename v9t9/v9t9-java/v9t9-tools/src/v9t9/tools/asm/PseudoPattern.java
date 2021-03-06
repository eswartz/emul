/*
  PseudoPattern.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class PseudoPattern {

	private final int numops;
	private final int realinst;
	private final LLOperand op1patt;
	private final LLOperand op2patt;
	private final LLOperand op3patt;

	/**
	 * @param numops
	 * @param realop
	 * @param op1patt
	 * @param op2patt
	 * @param op3patt
	 */
	public PseudoPattern(int numops, int realop, LLOperand op1patt,
			LLOperand op2patt, LLOperand op3patt) {
		this.numops = numops;
		this.realinst = realop;
		this.op1patt = op1patt;
		this.op2patt = op2patt;
		this.op3patt = op3patt;
	}

	/**
	 * @return the numops
	 */
	public int getNumops() {
		return numops;
	}

	/**
	 * @return the realop
	 */
	public int getRealop() {
		return realinst;
	}

	/**
	 * @return the op1patt
	 */
	public LLOperand getOp1() {
		return op1patt;
	}

	/**
	 * @return the op2patt
	 */
	public LLOperand getOp2() {
		return op2patt;
	}

	/**
	 * @return the op3patt
	 */
	public LLOperand getOp3() {
		return op3patt;
	}

	/**
	 * @return
	 */
	public int getInst() {
		return realinst;
	}

	
}
