package v9t9.tools.asm.assembler;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

public class AssemblerOperandParserStageF99 extends
		AssemblerOperandParserStage {

	public AssemblerOperandParserStageF99(Assembler assembler) {
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
		case AssemblerTokenizer.NUMBER:
		case AssemblerTokenizer.CHAR:
			tokenizer.pushBack();
			return parseNumber();
		}

		return null;
	}

}
