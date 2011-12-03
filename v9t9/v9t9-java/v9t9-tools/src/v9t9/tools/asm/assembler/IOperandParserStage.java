/**
 * 
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
