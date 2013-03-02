/*
  OperandParser.java

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
