package v9t9.tools.asm.assembler;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.BinaryOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegDecOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.ScaledRegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

public class AssemblerOperandParserStageMFP201 extends
		AssemblerOperandParserStage {

	public AssemblerOperandParserStageMFP201(Assembler assembler) {
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
			if (op instanceof RegIndOperand) {
				return new RegDecOperand(((RegIndOperand) op).getReg());
			}
			return new UnaryOperand('-', op);
		}
		case '*':
			return parseRegAddress();
		case '@':
			return parseAddress();
		case '#':
			// number
			AssemblerOperand op = parseNumber();
			return op;
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
		if (t == '(') {
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
							BinaryOperand rbop = (BinaryOperand) rightRes;
							AssemblerOperand sleft = ((BinaryOperand) right).getLeft();
							AssemblerOperand sright = ((BinaryOperand) right).getRight();
							if (rbop.getRight() instanceof LLRegisterOperand && !(rbop.getLeft() instanceof LLRegisterOperand) ) {
								AssemblerOperand tmp = sleft;
								sleft = sright;
								sright = tmp;
							}
							return new ScaledRegOffsOperand(expr, left, sleft, sright);
						}
					}
				}
				else if (bop.getKind() == '*') {
					AssemblerOperand rleft = bop.getLeft();
					AssemblerOperand rright = bop.getRight();
					if (rightRes instanceof LLRegisterOperand && !(leftRes instanceof LLRegisterOperand) ) {
						AssemblerOperand tmp = rleft;
						rleft = rright;
						rright = tmp;
						tmp = leftRes;
						leftRes = rightRes;
						rightRes = tmp;
					}
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
		} else {
			reg = new AddrOperand(expr);
			tokenizer.pushBack();
		}
		return reg;
	}
}
