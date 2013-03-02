/*
  InstructionParser.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.asm.IInstruction;

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
