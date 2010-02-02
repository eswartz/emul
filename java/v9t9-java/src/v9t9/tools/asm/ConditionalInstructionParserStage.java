/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.hl.SymbolOperand;
import v9t9.tools.llinst.ParseException;

/**
 * Parse uses of "if ... fi" directives
 * @author ejs
 *
 */
public class ConditionalInstructionParserStage implements IInstructionParserStage {

	private static final String ELSE = "else";
	private static final String FI = "fi";
	private static final String IF = "if";
	private static final String PFX = "#";
	private final OperandParser operandParser;
	private final Assembler assembler;
	
	public ConditionalInstructionParserStage(Assembler assembler, OperandParser operandParser) {
		this.assembler = assembler;
		this.operandParser = operandParser;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionParserStage#parse(java.lang.String)
	 */
	public IInstruction[] parse(String descr, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		
		try {
			tokenizer.match('#');
		} catch (ParseException e) {
			return null;
		}
		tokenizer.match(AssemblerTokenizer.ID);
		String directiveName = tokenizer.currentToken().toLowerCase();
		
		if (directiveName.equalsIgnoreCase(IF)) {
			parseIf(tokenizer, descr);
			return new IInstruction[0];
		} else if (directiveName.equalsIgnoreCase(ELSE)) {
			// from a non-conditional #else, skip this
			parseSkip(tokenizer, PFX+ELSE, descr);
			return new IInstruction[0];
		} else if (directiveName.equalsIgnoreCase(FI)) {
			// from a non-conditional #fi
			return new IInstruction[0];
		}
		else {
			throw new ParseException("Unknown directive: " + directiveName);
		}
	}

	/**
	 * Parse an if, syntax:
	 * <p>
	 * #if &lt;expr&gt;
	 * ... possibly skipped lines...
	 * #fi
	 * </p>
	 * @param tokenizer
	 * @param descr
	 */
	private void parseIf(AssemblerTokenizer tokenizer, String descr) throws ParseException {
		AssemblerOperand operand = (AssemblerOperand) operandParser.parse(tokenizer);
		
		// XXX: we don't resolve ANYTHING until the second pass, 
		// and we can't even peek at the current instructions to find a recent
		// EQU, therefore IF/etc can only work on special "symbolic constants"
		int value = 0;
		
		if (operand instanceof NumberOperand) {
			value = ((NumberOperand)operand).getValue();
		} else if (operand instanceof SymbolOperand) {
			Symbol symbol = ((SymbolOperand) operand).getSymbol();
			if (!symbol.isDefined())
				value = 0;
			else
				throw new ParseException("Cannot conditionalize on operand in "+PFX+IF+"; it must be defined by -Dsymbol=value or be undefined: " + operand);
		}
		else
			throw new ParseException("Cannot use this operand in "+PFX+IF+": " + operand);

		if (value == 0) {
			// skip
			parseSkip(tokenizer,PFX+IF, descr);
		}
	}

	/**
	 * Parse an else, syntax:
	 * <p>
	 * #else &lt;expr&gt;
	 * ... skipped lines...
	 * #fi
	 * </p>
	 * @param tokenizer
	 * @param descr
	 */
	private void parseSkip(AssemblerTokenizer tokenizer, String what, String descr) throws ParseException {
		String line;
		int level = 1;
		while ((line = assembler.getNextLine()) != null) {
			String trimmed = line.trim();
			if (trimmed.equals(PFX+FI)) {
				if (--level == 0)
					break;
			}
			if (trimmed.equals(PFX+ELSE)) {
				if (level == 1)
					break;
			}
			if (trimmed.equals(PFX+IF)) {
				++level;
			}
		}
		if (line == null)
			throw new ParseException("Unterminated " + what + " started at " + descr);
	}

}
