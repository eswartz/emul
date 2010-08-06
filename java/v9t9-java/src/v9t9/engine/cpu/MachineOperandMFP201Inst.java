/**
 * 
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 *
 */
public class MachineOperandMFP201Inst extends MachineOperandMFP201 {

	public RawInstruction inst;

	/**
	 * @param type
	 */
	public MachineOperandMFP201Inst(RawInstruction inst) {
		super(OP_INST);
		this.inst = inst;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperandMFP201#toString()
	 */
	@Override
	public String toString() {
		if (type == OP_INST)
			return inst.toString();
		return super.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inst == null) ? 0 : inst.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MachineOperandMFP201Inst other = (MachineOperandMFP201Inst) obj;
		if (inst == null) {
			if (other.inst != null)
				return false;
		} else if (!inst.equals(other.inst))
			return false;
		return true;
	}

	
}
