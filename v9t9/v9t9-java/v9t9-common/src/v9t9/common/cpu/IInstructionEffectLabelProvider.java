/*
  IInstructionEffectLabelProvider.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import v9t9.common.memory.IMemoryEntry;
import ejs.base.utils.HexUtils;

/**
 * Describes the relevant information for instructions
 * @author ejs
 *
 */
public interface IInstructionEffectLabelProvider {
	enum Role {
		UNKNOWN,
		SYMBOL,
		ADDRESS,
		INSTRUCTION,
		INPUT,	
		OUTPUT,
		OPERAND,
	}

	abstract class Column {
		public final String label;
		public final Role role;
		/** in characters */
		public final int width;
		
		public Column(String label, Role role, int width) {
			this.label = label;
			this.role = role;
			this.width = width;
		}
		/** Get the text for this column 
		 * @param block change block
		 * @param preExecute calculate for before execution or after
		 */
		abstract public String getText(ICpu cpu, ChangeBlock block, boolean preExecute);
		
	}
	
	class AddrColumn extends Column {

		public AddrColumn(int width) {
			super("Addr", Role.ADDRESS, width);
		}

		@Override
		public String getText(ICpu cpu, ChangeBlock block, boolean preExecute) {
			String addr = ">" + HexUtils.toHex4(block.inst.pc);
			return addr;
		}

	}

	class SymbolColumn extends Column {

		public SymbolColumn(int width) {
			super("Symbol", Role.SYMBOL, width);
		}

		@Override
		public String getText(ICpu cpu, ChangeBlock block, boolean preExecute) {
			IMemoryEntry entry = cpu.getConsole().getEntryAt(block.inst.pc);
			if (entry != null) { 
				String name = entry.lookupSymbol((short) (block.inst.pc & 0xfffe));
				if (name != null) {
					return name;
				}
			}
			return "";
		}

	}


	class InstructionColumn extends Column {

		public InstructionColumn(int width) {
			super("Instruction", Role.INSTRUCTION, width);
		}
		@Override
		public String getText(ICpu cpu, ChangeBlock block, boolean preExecute) {
			return block.inst.toString();
		}

	}

	
	/** Get the columns used to format the instruction 
	 *
	 * @return non-<code>null</code> array of columns
	 */
	Column[] getColumns();
}
