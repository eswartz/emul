/*
  IOperandParserStage.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler;

import v9t9.common.asm.IOperand;

/**
 * Implement an operand parser attempt.  
 * @author ejs
 *
 */
public interface IOperandParserStage {

	/**
	 * Try to match the tokens.  Throw an exception if the
	 * operand matches a pattern but is invalid; otherwise return null.
	 * @param tokenizer
	 * @return an operand
	 * @throws ParseException
	 */
	IOperand parse(AssemblerTokenizer tokenizer) throws ParseException;
}
