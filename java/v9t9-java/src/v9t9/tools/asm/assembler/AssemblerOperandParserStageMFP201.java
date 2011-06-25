package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.BinaryOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegDecOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.ScaledRegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

public class AssemblerOperandParserStageMFP201 extends
		AssemblerOperandParserStage {

	public AssemblerOperandParserStageMFP201(Assembler assembler) {
		super(assembler);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerOperandParserStage#parse(v9t9.tools.asm.assembler.AssemblerTokenizer)
	 */
	@Override
	public AssemblerOperand parse(AssemblerTokenizer tokenizer)
			throws ParseException {
		int t = tokenizer.nextToken();
		if (t == '#') {
			// lone number operand
			this.tokenizer = tokenizer;
			return parseNumber();
		} else {
			tokenizer.pushBack();
		}
		
		return super.parse(tokenizer);
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
			if (op instanceof RegIndOperand) {
				return new RegDecOperand(((RegIndOperand) op).getReg());
			}
			return new UnaryOperand('-', op);
		}
		case '*':
			return parseRegAddress();
		case '@':
			return parseAddress();
		case '&':
			return parseAbsoluteAddress();
		case AssemblerTokenizer.NUMBER:
		case AssemblerTokenizer.CHAR:
			tokenizer.pushBack();
			return parseNumber();
		}

		return null;
	}

	private AssemblerOperand parseRegAddress() throws ParseException {
		AssemblerOperand expr = parseExpr();
		AssemblerOperand reg = null;
		int t = tokenizer.nextToken();
		if (t == '+') {
			reg = new RegIncOperand(expr); 
		} else if (t == '-') {
			reg = new RegDecOperand(expr); 
		} else {
			reg = new RegIndOperand(expr);
			tokenizer.pushBack();
		}
		return reg;
	}

	private AssemblerOperand parseAddress() throws ParseException {
		AssemblerOperand expr = parseExpr();
		AssemblerOperand reg = null;
		int t = tokenizer.nextToken();
		if (t != '(') {
			tokenizer.pushBack();
			AssemblerOperand res = expr;
			try {
				res = expr.resolve(assembler, null); 
			} catch (ResolveException e) {
			}
			if (res instanceof NumberOperand || res instanceof LLImmedOperand) {
				tokenizer.pushBack();
				throw new ParseException("cannot specify addresses with '@'; use '&' or explicit '(PC)' suffix");
			}
			return new RegOffsOperand(expr, 
					new NumberOperand(MachineOperandMFP201.PC));
		}
	
		reg = parseExpr();
		t = tokenizer.nextToken();
		if (reg instanceof BinaryOperand) {
			// check for complex expr for LEA
			BinaryOperand bop = (BinaryOperand) reg;
			
			AssemblerOperand left = bop.getLeft();
			AssemblerOperand right = bop.getRight();
			AssemblerOperand leftRes = left;
			AssemblerOperand rightRes = right;
			try {
				leftRes = bop.getLeft().resolve(assembler, null);
			} catch (ResolveException e) {
				
			}
			try {
				rightRes = bop.getRight().resolve(assembler, null);
			} catch (ResolveException e) {
				
			}
			
			// move possible multiply to right
			if (leftRes instanceof BinaryOperand && !(rightRes instanceof BinaryOperand)) {
				AssemblerOperand tmp = leftRes;
				leftRes = rightRes;
				rightRes = tmp;
				tmp = left;
				left = right;
				right = tmp;
			}
			
			if (bop.getKind() == '+') {
				// Rx+Ry or Rx+Ry*s
				
				if (leftRes instanceof LLRegisterOperand) {
					if (rightRes instanceof LLRegisterOperand) {
						// Rx+Ry
						return new ScaledRegOffsOperand(expr, bop.getLeft(), bop.getRight(), new NumberOperand(1));
					}
					if (rightRes instanceof BinaryOperand && '*' == ((BinaryOperand) rightRes).getKind()) {
						// Rx+Ry*scale
						AssemblerOperand sleft = ((BinaryOperand) right).getLeft();
						AssemblerOperand sright = ((BinaryOperand) right).getRight();
						return new ScaledRegOffsOperand(expr, left, sleft, sright);
					}
				}
			}
			else if (bop.getKind() == '*') {
				AssemblerOperand rleft = bop.getLeft();
				AssemblerOperand rright = bop.getRight();
				if (leftRes instanceof LLRegisterOperand) {
					// Rx*scale
					return new ScaledRegOffsOperand(expr, null, rleft, rright);
				}
			}
			
			// not sure what this is!
		}
		if (t != ')') {
			throw new ParseException("Expected ')': " + tokenizer.currentToken());
		}
		
		// assume 'reg' will resolve to a number...
		return new RegOffsOperand(expr, reg);
	}
	
	private AssemblerOperand parseAbsoluteAddress() throws ParseException {
		AssemblerOperand expr = parseExpr();
		return new RegOffsOperand(expr, new NumberOperand(MachineOperandMFP201.SR));
	}
}
