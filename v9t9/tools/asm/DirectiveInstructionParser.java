/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.Instruction;
import v9t9.tools.llinst.ParseException;

/**
 * Parse directives
 * @author ejs
 *
 */
public class DirectiveInstructionParser implements IInstructionParserStage {

	private final Assembler assembler;

	public DirectiveInstructionParser(Assembler assembler) {
		this.assembler = assembler;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionParserStage#parse(java.lang.String)
	 */
	public Instruction[] parse(String string) throws ParseException {
		
		return null;
	}

}
