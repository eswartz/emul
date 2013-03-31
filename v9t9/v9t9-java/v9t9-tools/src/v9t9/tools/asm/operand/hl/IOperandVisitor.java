/*
  IOperandVisitor.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.hl;

/**
 * @author ejs
 *
 */
public interface IOperandVisitor {
	class Terminate extends RuntimeException { private static final long serialVersionUID = 1L; }
	boolean enterOperand(AssemblerOperand operand) throws Terminate;
	void exitOperand(AssemblerOperand operand) throws Terminate;
}
