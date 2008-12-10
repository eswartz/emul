/**
 * 
 */
package v9t9.tools.asm.operand.ll;

import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.utils.Utils;

/**
 * A low-level immediate
 * @author Ed
 *
 */
public class LLImmedOperand extends LLOperand {

	private int value;
	
	public LLImmedOperand(AssemblerOperand original, int value) {
		super(original);
		setValue(value);
	}

	public LLImmedOperand(int value) {
		this(null, value);
	}
	
	@Override
	public String toString() {
		return ">" + Utils.toHex4(value);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLImmedOperand other = (LLImmedOperand) obj;
		if (value != other.value)
			return false;
		return true;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.LLOperand#getSize()
	 */
	@Override
	public int getSize() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.LLOperand#hasImmediate()
	 */
	@Override
	public boolean hasImmediate() {
		return true;
	}

	@Override
	public int getImmediate() {
		return value;
	}

	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createImmediate(value);
	}
	
	@Override
	public boolean isConstant() {
		return getOriginal() == null;
	}
}
