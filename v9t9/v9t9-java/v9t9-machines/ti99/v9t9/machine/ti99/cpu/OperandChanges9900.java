/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpuState;

/**
 * @author ejs
 *
 */
public final class OperandChanges9900 {

//	private void pushFetchIndirectMemory(MachineOperandState state) {
//		if (state.mop.bIsReference) {
//			push(new CopyAddressToValue(state));
//		} else {
//			if (state.mop.byteop) {
//				push(new ReadByteIndirect(state));
//			} else {
//				push(new ReadWordIndirect(state));
//			}
//		}
//	}

	protected abstract static class BaseChangeElement implements IChangeElement {
//		private IFetchStateTracker parent;

//		@Override
//		public final IFetchStateTracker getParent() {
//			return parent;
//		}
//		@Override
//		public final void setParent(IFetchStateTracker parent) {
//			this.parent = parent;
//		}
		
		@Override
		public String toString() {
			String name = getClass().getSimpleName();
			if (name.isEmpty())
				name = getClass().getSuperclass().getSimpleName();
			return name;
		}
//		@Override
//		public int fetchRegister(int regX) {
//			return parent.fetchRegister(regX);
//		}
//		
//		@Override
//		public short fetchWord(int addr) {
//			return parent.fetchWord(addr);
//		}
//		
//		@Override
//		public byte fetchByte(int addr) {
//			return parent.fetchByte(addr);
//		}
	}
	
	protected abstract static class BaseOperandChangeElement extends BaseChangeElement {
		public final MachineOperandState state;
		protected ICpuState cpuState;
	
		public BaseOperandChangeElement(ICpuState cpu, MachineOperandState state) {
			super();
			this.cpuState = cpu;
			this.state = state;
		}
	
		protected abstract void doApply();
		protected void doRevert() {
			
		}
		
		@Override
		public final void apply() {
			int before = cpuState.getCycleCounts().getTotal();
			doApply();
			int after = cpuState.getCycleCounts().getTotal();
			state.cycles = after - before;
		}
		
		@Override
		public final void revert() {
			doRevert();
			cpuState.getCycleCounts().addLoad(-state.cycles);
		}
	}
	
	public final static class CopyAddressToValue extends BaseChangeElement {
		private MachineOperandState state;
		private short oldValue;
	
		protected CopyAddressToValue(MachineOperandState state) {
			this.state = state;
		}
	
		@Override
		public void apply() {
			oldValue = state.value;
			state.value = state.ea;
		}
		@Override
		public void revert() {
			state.value = oldValue;
		}
	}
	
	public final static class ReadRegister extends BaseOperandChangeElement {
		protected ReadRegister(CpuState9900 cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
		
		@Override
		protected void doApply() {
			if (state.mop.byteop)
				state.value = (short) (cpuState.getRegister(state.mop.val) >> 8);
			else
				state.value = (short) cpuState.getRegister(state.mop.val);
		}
	}
	
	public final static class ReadWord extends BaseOperandChangeElement {
		protected ReadWord(CpuState9900 cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
		
		@Override
		protected void doApply() {
			state.value = cpuState.getConsole().readWord(state.ea);
		}
	}
	
	public final static class ReadByte extends BaseOperandChangeElement {
		public ReadByte(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
	
		@Override
		protected void doApply() {
			state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
		}
	}
	
	public final static class ReadWordIndirect extends BaseOperandChangeElement {
		protected ReadWordIndirect(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
		
		@Override
		protected void doApply() {
			state.value = cpuState.getConsole().readWord(state.value);
		}
	}
	
	public final static class ReadByteIndirect extends BaseOperandChangeElement {
		public ReadByteIndirect(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
	
		@Override
		protected void doApply() {
			state.value = (short) (cpuState.getConsole().readByte(state.value) & 0xff);
		}
	}
	
	public final static class WriteWord extends BaseOperandChangeElement {
		public final short value;
		short prev;

		public WriteWord(ICpuState cpuState, MachineOperandState state, short value) {
			super(cpuState, state);
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.ChangeBlock9900.BaseOperandChangeElement#doApply()
		 */
		@Override
		protected void doApply() {
			prev = cpuState.getConsole().flatReadWord(state.ea);
			cpuState.getConsole().writeWord(state.ea, value);
		}

		@Override
		protected void doRevert() {
			super.doRevert();
			cpuState.getConsole().flatWriteWord(state.ea, prev);
		}

//		@Override
//		public int fetchRegister(int reg) {
//			int regAddr = (wp + reg * 2) & 0xfffe;
//			if (state.ea == regAddr) {
//				return (short) value;
//			}
//			return super.fetchRegister(reg);
//		}
//
//		@Override
//		public short fetchWord(int addrX) {
//			if ((addrX & 0xfffe) == (state.ea & 0xfffe)) {
//				return (short) value;
//			}
//			return super.fetchWord(addrX);
//		}
//
//		@Override
//		public byte fetchByte(int addrX) {
//			if ((addrX & 0xffff) == (state.ea & 0xffff)) {
//				// high byte
//				return (byte) (value >> 8);
//			}
//			if ((addrX & 0xffff) == (state.ea & 0xffff) + 1) {
//				// low byte
//				return (byte) value;
//			}
//			return super.fetchByte(addrX);
//		}
	}
	
	public final static class WriteByte extends BaseOperandChangeElement {
		public final byte value;
		byte prev;

		public WriteByte(ICpuState cpuState, MachineOperandState state, byte value) {
			super(cpuState, state);
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.ChangeBlock9900.BaseOperandChangeElement#doApply()
		 */
		@Override
		protected void doApply() {
			prev = cpuState.getConsole().flatReadByte(state.ea);
			cpuState.getConsole().writeByte(state.ea, value);
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.ChangeBlock9900.BaseOperandChangeElement#doRevert()
		 */
		@Override
		protected void doRevert() {
			super.doRevert();
			cpuState.getConsole().flatWriteByte(state.ea, prev);
		}

//		@Override
//		public int fetchRegister(int reg) {
//			int regAddr = (wp + reg * 2) & 0xfffe;
//			if (state.ea == regAddr) {
//				byte lo = fetchByte(regAddr + 1);
//				return (value << 8) | (lo & 0xff);
//			}
//			if (state.ea == regAddr + 1) {
//				byte hi = fetchByte(regAddr);
//				return (hi << 8) | (value & 0xff);
//			}
//			return super.fetchRegister(reg);
//		}
//
//		@Override
//		public short fetchWord(int addrX) {
//			if ((addrX & 0xfffe) == (state.ea & 0xfffe)) {
//				return value;
//			}
//			return super.fetchWord(addrX);
//		}
//
//		@Override
//		public byte fetchByte(int addrX) {
//			if ((addrX & 0xffff) == (state.ea & 0xffff)) {
//				// high byte
//				return (byte) (value >> 8);
//			}
//			if ((addrX & 0xffff) == (state.ea & 0xffff) + 1) {
//				// low byte
//				return (byte) value;
//			}
//			return super.fetchByte(addrX);
//		}
	}
	
	public final static class CalculateOffset extends BaseOperandChangeElement {
		public CalculateOffset(ICpuState cpu, MachineOperandState state) {
			super(cpu, state);
		}
	
		@Override
		protected void doApply() {
			state.value = (short) ((cpuState.getConsole().readWord(state.ea) >> 1) + state.mop.val);
		}
	}
	
	public final static class CalculateShift extends BaseOperandChangeElement {
		public CalculateShift(ICpuState cpu, MachineOperandState state) {
			super(cpu, state);
		}
	
		@Override
		protected void doApply() {
			state.value = (short) (cpuState.getConsole().readWord(state.ea) & 0xf);
		    if (state.value == 0) {
		    	state.value = 16;
			}
		}
	}
	
	public final static class WriteRegister extends BaseChangeElement {
		public final int reg;
		public final int value;
		int prev;
		private ICpuState cpuState;

		public WriteRegister(ICpuState cpuState, int reg, int value) {
			this.cpuState = cpuState;
			this.reg = reg;
			this.value = value;
		}

		@Override
		public void apply() {
			prev = cpuState.getRegister(reg);
			cpuState.setRegister(reg, value);
		}

		@Override
		public void revert() {
			cpuState.setRegister(reg, prev);
		}

//		@Override
//		public int fetchRegister(int regX) {
//			return reg == regX ? value : super.fetchRegister(regX);
//		}
//
//		@Override
//		public short fetchWord(int addr) {
//			int regAddr = (wp + reg * 2) & 0xfffe;
//			addr &= 0xfffe;
//			if (addr == regAddr) {
//				return (short) value;
//			}
//			return super.fetchWord(addr);
//		}
//
//		@Override
//		public byte fetchByte(int addr) {
//			int regAddr = (wp + reg * 2) & 0xfffe;
//			addr &= 0xfffe;
//			if (addr == regAddr) {
//				// high byte
//				return (byte) (value >> 8);
//			}
//			if (addr == regAddr + 1) {
//				// low byte
//				return (byte) (value);
//			}
//			return super.fetchByte(addr);
//		}
	}

	public final static class ReadIndirectRegister extends BaseOperandChangeElement {
		int prevReg;
	
		public ReadIndirectRegister(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
	
		@Override
		protected void doApply() {
			short ea = (short) (((CpuState9900) cpuState).getWP() + (state.mop.val << 1));
			prevReg = cpuState.getConsole().readWord(ea);
			if (state.mop.byteop)
				state.value = cpuState.getConsole().readByte(ea);
			else
				state.value = cpuState.getConsole().readWord(ea);
	
		}
	}
	
	public final static class ReadIncrementRegister extends BaseOperandChangeElement {
		int prevReg;
		private short ea;
	
		public ReadIncrementRegister(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
	
		@Override
		protected void doApply() {
			ea = (short) (((CpuState9900) cpuState).getWP() + (state.mop.val << 1));
			prevReg = cpuState.getConsole().flatReadWord(ea);
			state.value = cpuState.getConsole().readWord(prevReg);
			cpuState.getConsole().writeWord(ea, (short)(prevReg + (state.mop.byteop ? 1 : 2)));
	
		}
		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.ChangeBlock9900.BaseOperandChangeElement#doRevert()
		 */
		@Override
		protected void doRevert() {
			super.doRevert();
			cpuState.getConsole().flatWriteWord(ea, (short) prevReg);
		}
	}
	
	/**
	 * @author ejs
	 *
	 */
	public final static class ReadRegisterOffset extends BaseOperandChangeElement {
		public ReadRegisterOffset(ICpuState cpuState, MachineOperandState state) {
			super(cpuState, state);
		}
	
		@Override
		protected void doApply() {
			short ea = state.mop.immed; 
    		if (state.mop.val != 0) {
    			int offs = cpuState.getRegister(state.mop.val);
    			ea += offs;
    		}
			state.value = cpuState.getConsole().readWord(ea);
		}
		
	}
}