/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.llinst.ParseException;

/**
 * Attempt to parse instruction(s) of a given type.
 * @author ejs
 *
 */
public interface IInstructionParserStage {

	/** Try to parse one or more instructions from a string. 
	 * @param descr TODO
	 * @param string the text 
	 * 
	 * @return instructions generated, or <code>null</code> if unhandled
	 * @throws ParseException if string parsed but errorneous
	 */
	IInstruction[] parse(String descr, String string) throws ParseException;
}
