/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.MachineOperandState;
import v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator;

/**
 * @author ejs
 *
 */
public final class Changes {

	protected abstract static class BaseChangeElement implements IChangeElement {
		public final ChangeBlock9900 changeBlock;

		public BaseChangeElement(ChangeBlock9900 changeBlock) {
			this.changeBlock = changeBlock;
		}
		@Override
		public String toString() {
			String name = getClass().getSimpleName();
			if (name.isEmpty())
				name = getClass().getSuperclass().getSimpleName();
			return name;
		}
		@Override
		public int hashCode() {
			int result = 1;
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
//			BaseChangeElement other = (BaseChangeElement) obj;
			return true;
		}
		
	}
	
	protected abstract static class BaseOperandChangeElement extends BaseChangeElement {
		public final MachineOperandState state;
	
		public BaseOperandChangeElement(ChangeBlock9900 changeBlock, MachineOperandState state) {
			super(changeBlock);
			this.state = state;
		}
		
		
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((state == null) ? 0 : state.hashCode());
			return result;
		}



		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			BaseOperandChangeElement other = (BaseOperandChangeElement) obj;
			if (state == null) {
				if (other.state != null)
					return false;
			} else if (!state.equals(other.state))
				return false;
			return true;
		}



		protected abstract void doApply(ICpuState cpuState);
		protected void doRevert(ICpuState cpuState) {
			
		}
		
		@Override
		public final void apply(ICpuState cpuState) {
			//int before = cpuState.getCycleCounts().getTotal();
			doApply(cpuState);
			//int after = cpuState.getCycleCounts().getTotal();
			//state.cycles = after - before;
		}
		
		@Override
		public final void revert(ICpuState cpuState) {
			doRevert(cpuState);
			//cpuState.getCycleCounts().addLoad(-state.cycles);
		}
	}
	
//	public final static class CopyAddressToValue extends BaseChangeElement {
//		private MachineOperandState state;
//		private short oldValue;
//	
//		protected CopyAddressToValue(MachineOperandState state) {
//			this.state = state;
//		}
//	
//		@Override
//		public void apply(ICpuState cpuState) {
//			oldValue = state.value;
//			state.value = state.ea;
//		}
//		@Override
//		public void revert(ICpuState cpuState) {
//			state.value = oldValue;
//		}
//	}
	
	public final static class ReadRegister extends BaseOperandChangeElement {
		protected ReadRegister(ChangeBlock9900 changeBlock, MachineOperandState state) {
			super(changeBlock, state);
		}
		
		@Override
		protected void doApply(ICpuState cpuState) {
			changeBlock.counts.addFetch(0);
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.ea = (short) (((CpuState9900) cpuState).getWP() + (mop.val << 1));
			if (mop.byteop)
				state.prev = state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
			else
				state.prev = state.value = cpuState.getConsole().readWord(state.ea);
		}
	}

//	public final static class WriteRegister extends BaseOperandChangeElement {
//		protected WriteRegister(MachineOperandState state) {
//			super(state);
//		}
//		
//		@Override
//		protected void doApply(ICpuState cpuState) {
//			MachineOperand9900 mop = (MachineOperand9900) state.mop;
//			state.ea = (short) (((CpuState9900) cpuState).getWP() + (mop.val << 1));
//			if (mop.byteop)
//				cpuState.getConsole().writeByte(state.ea, (byte) state.value);
//			else
//				cpuState.getConsole().writeWord(state.ea, state.value);
//		}
//	}
//	
	public final static class CalculateCruOffset extends BaseOperandChangeElement {
		public CalculateCruOffset(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(ICpuState cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.prev = state.value = (short) ((cpuState.getRegister(12) >> 1) + mop.val);
		}
	}
	
	public final static class CalculateShift extends BaseOperandChangeElement {
		public CalculateShift(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(ICpuState cpuState) {
			changeBlock.counts.addFetch(8);

			state.value = (short) (cpuState.getRegister(0) & 0xf);
		    if (state.value == 0) {
		    	state.value = 16;
			}
		    state.prev = state.value;
		}
	}
	
	public final static class AdvancePC extends BaseChangeElement {
		public final int value;
		private int prev;
		private int cycles;

		public AdvancePC(ChangeBlock9900 changes, int value, int cycles) {
			super(changes);
			this.value = value;
			this.cycles = cycles;
		}

		@Override
		public void apply(ICpuState cpuState) {
			prev = cpuState.getRegister(Cpu9900.REG_PC);
			cpuState.setRegister(Cpu9900.REG_PC, prev + value);
			changeBlock.counts.addExecute(cycles);
		}

		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(Cpu9900.REG_PC, prev);
			changeBlock.counts.addExecute(-cycles);
		}
	}


	public final static class ReadIndirectRegister extends BaseOperandChangeElement {
		public ReadIndirectRegister(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(ICpuState cpuState) {
			changeBlock.counts.addFetch(4);
			
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.ea = (short) (((CpuState9900) cpuState).getWP() + (mop.val << 1));
			
			// read register value
			state.ea = cpuState.getConsole().readWord(state.ea);
			
			if (mop.bIsReference) {
				state.value = state.ea;
			} else {
				if (mop.byteop)
					state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
				else
					state.value = cpuState.getConsole().readWord(state.ea);
			}
			state.prev = state.value;
		}
	}
	
	public final static class ReadIncrementRegister extends BaseOperandChangeElement {
		public ReadIncrementRegister(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(ICpuState cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			
			changeBlock.counts.addFetch(4);
			changeBlock.counts.addFetch(mop.byteop ? 2 : 4);
			
			short regAddr = (short) (((CpuState9900) cpuState).getWP() + (mop.val << 1));
			state.prev = cpuState.getConsole().flatReadWord(regAddr);
			state.ea = state.prev;
			if (mop.bIsReference) {
    			state.value = state.ea;
    		} else {
    			if (mop.byteop)
    				state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
    			else
    				state.value = cpuState.getConsole().readWord(state.ea);
    		}
			cpuState.getConsole().writeWord(regAddr, (short)(state.prev + (mop.byteop ? 1 : 2)));
	
		}
		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.ChangeBlock9900.BaseOperandChangeElement#doRevert()
		 */
		@Override
		protected void doRevert(ICpuState cpuState) {
			super.doRevert(cpuState);
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short regAddr = (short) (((CpuState9900) cpuState).getWP() + (mop.val << 1));
			cpuState.getConsole().flatWriteWord(regAddr, state.prev);
		}
	}
	
	public final static class ReadRegisterOffset extends BaseOperandChangeElement {
		public ReadRegisterOffset(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(ICpuState cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			
    		changeBlock.counts.addFetch(8);
    		
			state.ea = mop.immed; 
    		if (mop.val != 0) {
    			int offs = cpuState.getRegister(mop.val);
    			state.ea += offs;
    		}
    		if (mop.bIsReference) {
    			state.value = state.ea;
    		} else {
    			if (mop.byteop)
    				state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
    			else
    				state.value = cpuState.getConsole().readWord(state.ea);
    		}
    		state.prev = state.value;
		}
		
	}
	
	public static class SaveContext implements IChangeElement {
		protected short pc, wp, st;
		
		@Override
		public void apply(ICpuState cpuState) {
			pc = cpuState.getPC();
			wp = ((CpuState9900) cpuState).getWP();
			st = cpuState.getST();
		}

		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setPC(pc);
			((CpuState9900) cpuState).setWP(wp);
			cpuState.setST(st);
		}

	}
	
	public static abstract class SwitchContext extends SaveContext {
		protected int r13, r14, r15;
		
		public SwitchContext() {
		}
		
		protected abstract short getVectorAddress();
		
		@Override
		public void apply(ICpuState cpuState) {
			super.apply(cpuState);

			short vec = getVectorAddress();
			short newWP = cpuState.getConsole().readWord(vec);
			short newPC = cpuState.getConsole().readWord(vec + 2);

			r13 = cpuState.getConsole().readWord(newWP + 13*2);
			r14 = cpuState.getConsole().readWord(newWP + 14*2);
			r15 = cpuState.getConsole().readWord(newWP + 15*2);

			((CpuState9900) cpuState).contextSwitch(newWP, newPC);
			
		}
		
		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(13, r13);
			cpuState.setRegister(14, r14);
			cpuState.setRegister(15, r15);
			super.revert(cpuState);
		}
	}
	public static class Blwp extends SwitchContext {
		private MachineOperandState state;
		
		public Blwp(MachineOperandState state) {
			super();
			this.state = state;
		}
		@Override
		protected short getVectorAddress() {
			return state.ea;
		}
	}
	public static class Xop extends SwitchContext {
		protected int r11;
		private MachineOperandState xopNumber;
		private MachineOperandState op;
		
		public Xop(MachineOperandState op, MachineOperandState xopNumber) {
			super();
			this.op = op;
			this.xopNumber = xopNumber;
		}
		
		@Override
		protected short getVectorAddress() {
			return (short) (0x40 + xopNumber.value * 4);
		}
		@Override
		public void apply(ICpuState cpuState) {
			super.apply(cpuState);
			r11 = cpuState.getRegister(11);
			cpuState.setRegister(11, op.ea);
			cpuState.setST((short) (cpuState.getST() | Status9900.ST_X));
		}
		
		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(11, r11);
			super.revert(cpuState);
		}
	}
	
	public static class RestoreContext extends SaveContext {
		@Override
		public void apply(ICpuState cpuState) {
			super.apply(cpuState);
			cpuState.setPC((short) cpuState.getRegister(14));
			cpuState.setST((short) cpuState.getRegister(15));
			((CpuState9900) cpuState).setWP((short) cpuState.getRegister(13));
		}
	}
	public static final class WriteResult extends BaseOperandChangeElement {
		public WriteResult(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}

		@Override
		protected void doApply(ICpuState cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
            if (mop.byteop) {
            	state.prev = cpuState.getConsole().flatReadByte(state.ea);
				cpuState.getConsole().writeByte(state.ea, (byte) state.value);
			} else {
				state.prev = cpuState.getConsole().flatReadWord(state.ea);
				cpuState.getConsole().writeWord(state.ea, state.value);
//				if (inst.getInst() == InstTableCommon.Iticks) {
//					cpuState.getConsole().writeWord(state.ea + 2, mopState2.value);
//				}
			}
		}
		
		@Override
		protected void doRevert(ICpuState cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
            if (mop.byteop) {
				cpuState.getConsole().writeByte(state.ea, (byte) state.prev);
			} else {
				cpuState.getConsole().writeWord(state.ea, state.prev);
			}
            super.doRevert(cpuState);
		}
		
	}

	public static class Flush implements IChangeElement {
		public final ChangeBlock9900 changes;
		private ICycleCalculator cycleCalculator;

		public Flush(ChangeBlock9900 changes) {
			this.changes = changes;
			this.cycleCalculator = InstTable9900.instCycles.get(changes.inst.getInst());
		}

		/* (non-Javadoc)
		 * @see v9t9.common.cpu.IChangeElement#apply(v9t9.common.cpu.ICpuState)
		 */
		@Override
		public void apply(ICpuState cpuState) {
			cycleCalculator.addCycles(changes);

			//changes.executeCycles = changes.counts.getTotal() - changes.cyclesAtStart;
			
			if (changes.inst.getInst() != Inst9900.Irtwp)
				changes.cpuState.getStatus().flatten();	// ensure status flushed
		}

		/* (non-Javadoc)
		 * @see v9t9.common.cpu.IChangeElement#revert(v9t9.common.cpu.ICpuState)
		 */
		@Override
		public void revert(ICpuState cpuState) {
			//changes.cpu.applyCycles(-(changes.fetchCycles + changes.executeCycles));
		}

	}


}