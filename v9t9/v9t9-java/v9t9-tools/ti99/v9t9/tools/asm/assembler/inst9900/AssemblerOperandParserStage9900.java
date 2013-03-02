/*
  AssemblerOperandParserStage9900.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.inst9900;

import v9t9.tools.asm.assembler.AssemblerOperandParserStage;
import v9t9.tools.asm.assembler.AssemblerTokenizer;
import v9t9.tools.asm.assembler.Equate;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.StringOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;

public class AssemblerOperandParserStage9900 extends
		AssemblerOperandParserStage {

	public AssemblerOperandParserStage9900(IAssembler assembler) {
		super(assembler);
	}

	@Override
	protected AssemblerOperand parseTargetSpecificOperand(int t)
			throws ParseException {

		switch (t) {
		case '+': {
			AssemblerOperand op = parseFactor();
			if (op instanceof NumberOperand)
				return op;
			if (op instanceof RegIndOperand) {
				return new RegIncOperand(((RegIndOperand) op).getReg());
			}
			throw new ParseException("Suspicious use of + for " + op);
		}
		case '-': { 
			AssemblerOperand op = parseFactor();
			return new UnaryOperand('-', op);
		}
		case '*':
			return parseRegIndInc();
		case '@':
			return parseAddr();
		case '#': {
			// const table
			AssemblerOperand op = parseFactor();
			return new ConstPoolRefOperand(op);
		}
		case AssemblerTokenizer.NUMBER:
		case AssemblerTokenizer.CHAR:
			tokenizer.pushBack();
			return parseNumber();

		case AssemblerTokenizer.ID:
			Symbol symbol = assembler.referenceSymbol(tokenizer.getString());
			if (symbol instanceof Equate) {
				return ((Equate) symbol).getValue();
			}
			return new SymbolOperand(symbol);
		case AssemblerTokenizer.STRING:
			return new StringOperand(tokenizer.getString());
			
		case AssemblerTokenizer.EOF:
			throw new ParseException("Unexpected end of line");
		}

		return null;
	}

	private AssemblerOperand parseRegIndInc() throws ParseException {
		AssemblerOperand reg = parseFactor();
		int t = tokenizer.nextToken();
		if (t == '+') {
			reg = new RegIncOperand(reg); 
		} else {
			reg = new RegIndOperand(reg); 
			tokenizer.pushBack();
		}
		return reg;
	}

	private AssemblerOperand parseAddr() throws ParseException {
		AssemblerOperand addr = parseExpr();
		
		int t = tokenizer.nextToken();
		if (t == '(') {
			AssemblerOperand reg = parseFactor();
			t = tokenizer.nextToken();
			if (t != ')') {
				throw new ParseException("Expected ')': " + tokenizer.currentToken());
			}
			
			return new RegOffsOperand(addr, reg);
		} else {
			tokenizer.pushBack();
			return new AddrOperand(addr);
		}
	}


}
