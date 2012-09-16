/**
 * 
 */
package v9t9.machine.f99b.cpu;

import v9t9.common.cpu.IInstructionEffectLabelProvider;

/**
 * @author ejs
 *
 */
public class InstructionEffectLabelProviderF99b implements
		IInstructionEffectLabelProvider {

	static final Column[] columns = {
		new SymbolColumn(12),
		new AddrColumn(5),
		new InstructionColumn(24),
	};
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionEffectLabelProvider#getColumns()
	 */
	@Override
	public Column[] getColumns() {
		return columns;
	}
}
