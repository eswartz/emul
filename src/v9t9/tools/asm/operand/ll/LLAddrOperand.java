/**
 * 
 */
package v9t9.tools.asm.operand.ll;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLAddrOperand extends LLOperand implements Operand {

	int address;
	
	public LLAddrOperand(AssemblerOperand original, int value) {
		super(original);
		setAddress(value);
	}
	
	@Override
	public String toString() {
		return "@>" + HexUtils.toHex4(address);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
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
		LLAddrOperand other = (LLAddrOperand) obj;
		if (address != other.address)
			return false;
		return true;
	}



	public int getAddress() {
		return address;
	}



	public void setAddress(int value) {
		this.address = value;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getImmediate()
	 */
	@Override
	public int getImmediate() {
		return address;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getSize()
	 */
	@Override
	public int getSize() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#hasImmediate()
	 */
	@Override
	public boolean hasImmediate() {
		return true;
	}

	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createGeneralOperand(MachineOperand.OP_ADDR, (short) 0, (short) address);
	}

}
