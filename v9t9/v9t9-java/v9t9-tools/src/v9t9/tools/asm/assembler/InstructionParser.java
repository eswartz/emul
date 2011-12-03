/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.util.ArrayList;
import java.util.List;

import v9t9.engine.cpu.IInstruction;

/**
 * This class parses instructions one at a time.
 * @author ejs
 *
 */
public class InstructionParser {
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

	public IInstruction[] parse(String descr, String line) throws ParseException {
		for (IInstructionParserStage stage : stages) {
			IInstruction[] insts = stage.parse(descr, line);
			if (insts != null)
				return insts;
		}
		throw new ParseException("Unknown instruction or directive: " + line);
	}
}
