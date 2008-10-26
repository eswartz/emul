/**
 * 
 */
package v9t9.tools.asm;

import java.util.ArrayList;
import java.util.List;

import v9t9.engine.cpu.Instruction;
import v9t9.tools.llinst.ParseException;

/**
 * This class parses instructions one at a time.
 * @author ejs
 *
 */
public class InstructionParser {

	public static final InstructionParser STANDARD = new InstructionParser();
	
	static {
		STANDARD.appendStage(new StandardInstructionParserStage());
	}

	private List<IInstructionParserStage> stages = new ArrayList<IInstructionParserStage>(1);
	
	/** 
	 * Create a new instruction parser that only handles standard
	 * instructions, e.g. from a disassembly without labels.
	 */
	public InstructionParser() {
	}
	
	public void appendStage(IInstructionParserStage stage) {
		stages.add(stage);
	}

	public Instruction[] parse(String line) throws ParseException {
		for (IInstructionParserStage stage : stages) {
			Instruction[] insts = stage.parse(line);
			if (insts != null)
				return insts;
		}
		throw new ParseException("Unknown instruction or directive: " + line);
	}
}
