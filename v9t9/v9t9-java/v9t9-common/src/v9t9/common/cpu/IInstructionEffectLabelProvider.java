/**
 * 
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
		 * @param before instruction state before invocation
		 * @param after instruction state after invocation (may be <code>null</code> to
		 * reflect state before execution)
		 */
		abstract public String getText(InstructionWorkBlock before, InstructionWorkBlock after);
		
	}
	
	class AddrColumn extends Column {

		public AddrColumn(int width) {
			super("Addr", Role.ADDRESS, width);
		}

		@Override
		public String getText(InstructionWorkBlock before,
				InstructionWorkBlock after) {
			String addr = ">" + HexUtils.toHex4(before.inst.pc);
			return addr;
		}

	}

	class SymbolColumn extends Column {

		public SymbolColumn(int width) {
			super("Symbol", Role.SYMBOL, width);
		}

		@Override
		public String getText(InstructionWorkBlock before,
				InstructionWorkBlock after) {
			IMemoryEntry entry = before.domain.getEntryAt(before.inst.pc);
			if (entry != null) { 
				String name = entry.lookupSymbol((short) (before.inst.pc & 0xfffe));
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
		public String getText(InstructionWorkBlock before,
				InstructionWorkBlock after) {
			return before.inst.toString();
		}

	}

	
	/** Get the columns used to format the instruction 
	 *
	 * @return non-<code>null</code> array of columns
	 */
	Column[] getColumns();
}
