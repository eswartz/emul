/*
  OperandParser.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.asm.IOperand;

/**
 * Parse a 9900 operand.
 * @author ejs
 *
 */
public class OperandParser {

	private List<IOperandParserStage> stages = new ArrayList<IOperandParserStage>(1);
	
	/** Create a standard operand parser that understands
	 * raw operands (no symbols).
	 */
	public OperandParser() {
	}
	
	public void prependStage(IOperandParserStage stage) {
		stages.add(0, stage);
	}
	public void appendStage(IOperandParserStage stage) {
		stages.add(stage);
	}

	public IOperand parse(AssemblerTokenizer tokenizer) throws ParseException {
		TokenizerState state = tokenizer.getState();
		for (IOperandParserStage stage : stages) {
			IOperand op = stage.parse(tokenizer);
			if (op != null)
				return op;
			tokenizer.setState(state);
		}
		throw new ParseException("Unknown operand");
    }

}
