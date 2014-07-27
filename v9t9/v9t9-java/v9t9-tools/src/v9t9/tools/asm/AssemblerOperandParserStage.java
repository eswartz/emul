/*
  AssemblerOperandParserStage.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.BinaryOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.hl.PcRelativeOperand;
import v9t9.tools.asm.operand.hl.StringOperand;
import v9t9.tools.asm.operand.hl.SymbolOperand;
import v9t9.tools.asm.operand.hl.UnaryOperand;

/**
 * Parse operands for an assembler and generate a high level {@link AssemblerOperand}.
 * This handles expressions, symbols, and forward references.
 * @author ejs
 *
 */
public abstract class AssemblerOperandParserStage implements IOperandParserStage {

	protected final IAssembler assembler;
	protected AssemblerTokenizer tokenizer;

	public AssemblerOperandParserStage(IAssembler assembler) {
		this.assembler = assembler;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IOperandParserStage#parse(java.lang.String)
	 */
	public AssemblerOperand parse(AssemblerTokenizer tokenizer) throws ParseException {
		this.tokenizer = tokenizer;

		AssemblerOperand op = parseExpr();
		return op;
	}

	/*
	 * cond ExprRest 
	 * | '>' cond ExprRest
	 * | '<' cond ExprRest
	 * | '==' cond ExprRest
	 * | '>=' cond ExprRest
	 * | '<=' cond ExprRest
	 */
	protected AssemblerOperand parseExpr() throws ParseException {
		AssemblerOperand op = parseExprRest(parseCond());
		return op;
	}

	private AssemblerOperand parseExprRest(AssemblerOperand cond) throws ParseException {
		int t = tokenizer.nextToken();
		if (t == '>' || t == '<' || t == '=') {
			int nt = tokenizer.nextToken();
			if (nt != '=') {
				tokenizer.pushBack();
			} else {
				switch (t) {
				case '>':
					t = '≥';	 // 0x2265 greater-than or equal to
					break;
				case '<':
					t = '≤';	 // 0x2264 less-than or equal to
					break;
				case '=':
					// same
					break;
				}
			}
			
			AssemblerOperand op = parseExprRest(new BinaryOperand(t, cond, parseCond()));
			return op;
		}
		tokenizer.pushBack();
		return cond;
	}
	
	protected AssemblerOperand parseCond() throws ParseException {
		AssemblerOperand op = parseCondRest(parseTerm());
		return op;
	}

	private AssemblerOperand parseCondRest(AssemblerOperand term) throws ParseException {
		TokenizerState state = tokenizer.getState();
		int t = tokenizer.nextToken();
		if (t == '+' || t == '-') {
			// HACK: the reg inc/dec format has a trailing + or -
			try {
				AssemblerOperand op = parseExprRest(new BinaryOperand(t, term, parseCond()));
				return op;
			} catch (ParseException e) {
			}
		}
		tokenizer.setState(state);
		return term;
	}

	private AssemblerOperand parseTerm() throws ParseException {
		AssemblerOperand op = parseTermRest(parseFactor());
		return op;
	}

	private AssemblerOperand parseTermRest(AssemblerOperand factor) throws ParseException {
		int t = tokenizer.nextToken();
		if (t == '*' || t == '/') {
			AssemblerOperand op = parseExprRest(new BinaryOperand(t, factor, parseFactor()));
			return op;
		}
		tokenizer.pushBack();
		return factor;
	}

	protected abstract AssemblerOperand parseTargetSpecificOperand(int token) throws ParseException;
	
	protected AssemblerOperand parseFactor() throws ParseException {
		int t = tokenizer.nextToken();
		AssemblerOperand top = parseTargetSpecificOperand(t);
		if (top != null)
			return top;
		
		switch (t) {
		case '+': {
			AssemblerOperand op = parseFactor();
			if (op instanceof NumberOperand)
				return op;
			throw new ParseException("Suspicious use of + for " + op);
		}
		case '-': { 
			AssemblerOperand op = parseFactor();
			return new UnaryOperand('-', op);
		}
		case '$':
			// either expr like "$ + foo" or "$ 2 +"
			return parseJumpTarget();
		case '(': {
			AssemblerOperand op = parseExpr();
			t = tokenizer.nextToken();
			if (t != ')')
				throw new ParseException("Expected close paren");
			return op;
		}
		
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

		throw new ParseException("Unknown token: " + tokenizer.currentToken());

	}

	protected AssemblerOperand parseNumber() throws ParseException { 
		int t = tokenizer.nextToken();
		
		switch (t) {
		case AssemblerTokenizer.NUMBER:
			return makeNumber(tokenizer.getNumber());
	
		case AssemblerTokenizer.CHAR: {
			String ch = tokenizer.getString();
			if (ch.length() == 1)
				return makeNumber((char)ch.charAt(0));
			else if (ch.length() == 2)
				return makeNumber((ch.charAt(0) << 8) | (ch.charAt(1) & 0xff));
			else
				throw new ParseException("Char literal is wrong length: " + ch);
			}
		
		default:
			throw new ParseException("Unknown number: " + tokenizer.currentToken());
		}
	}
	private AssemblerOperand parseJumpTarget() throws ParseException {
		int t = tokenizer.nextToken();
		if (t == AssemblerTokenizer.NUMBER) {
			String target = tokenizer.currentToken();
			Symbol symbol;
			t = tokenizer.nextToken();
			String labelName = "$" + target;
			if (t == '+') {
				symbol = assembler.findForwardLocalLabel(labelName);
			} else {
				symbol = assembler.findBackwardLocalLabel(labelName);
				// back ref: use previous one
				//symbol = assembler.getSymbolTable().findSymbol(labelName);
				//if (symbol == null)
				//	throw new ParseException("No previous label " + labelName);
			}
			return new SymbolOperand(symbol);
		} else {
			tokenizer.pushBack();
			return new PcRelativeOperand();
		}
	}

	protected AssemblerOperand makeNumber(int i) {
		return new NumberOperand(i);
	}


}
