/*
  Changes.java

  (c) 2014-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import ejs.base.utils.HexUtils;
import v9t9.common.asm.IOperand;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IOperandChangeElement;
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
		
//		@Override
//		public IChangeElement clone() {
//			try {
//				return (IChangeElement) super.clone();
//			} catch (CloneNotSupportedException e) {
//				assert false;
//				return null;
//			}
//		}
		
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
	
	public abstract static class BaseOperandChangeElement extends BaseChangeElement implements IOperandChangeElement {
		public final MachineOperandState state;
		
		public BaseOperandChangeElement(ChangeBlock9900 changeBlock, MachineOperandState state) {
			super(changeBlock);
			this.state = state;
		}
		
//		@Override
//		public IChangeElement clone() {
//			BaseOperandChangeElement c = (BaseOperandChangeElement) super.clone();
//			c.state = state.clone();
//			return c;
//		}
		
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



		protected abstract void doApply(CpuState9900 cpuState);
		protected void doRevert(CpuState9900 cpuState) {
			
		}
		
		@Override
		public final void apply(ICpuState cpuState) {
			doApply((CpuState9900) cpuState);
		}
		
		@Override
		public final void revert(ICpuState cpuState) {
			doRevert((CpuState9900) cpuState);
		}
	}
	
	protected static abstract class BaseRegisterOperandChangeElement extends BaseOperandChangeElement {
		protected BaseRegisterOperandChangeElement(ChangeBlock9900 changeBlock, MachineOperandState state) {
			super(changeBlock, state);
		}
		
		abstract protected short preExecuteEA();
		
		@Override
		public final String format(IOperand op, boolean preExecute) {
			if (op != state.mop)
				return null;

			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short ea;
			String str;
			if (preExecute) {
				ea = preExecuteEA();
				
				if (mop.bIsReference) {
					return ">" + HexUtils.toHex4(ea);
				} else {
					if (mop.byteop)
						str = HexUtils.toHex2(changeBlock.cpuState.getConsole().flatReadByte(ea) & 0xff);
					else
						str = HexUtils.toHex4(changeBlock.cpuState.getConsole().flatReadWord(ea));
				}
			} else {
				ea = state.ea;
				if (mop.bIsReference) {
					return ">" + HexUtils.toHex4(ea);
				} else {
					if (mop.byteop)
						str = HexUtils.toHex2(state.value);
					else
						str = HexUtils.toHex4(state.value);
				}
			}				
			return ">" + str + " (@" + HexUtils.toHex4(ea) + ")";
		}
	}
	public final static class ReadRegister extends BaseRegisterOperandChangeElement {
		protected ReadRegister(ChangeBlock9900 changeBlock, MachineOperandState state) {
			super(changeBlock, state);
		}
		
		@Override
		protected void doApply(CpuState9900 cpuState) {
			changeBlock.counts.addFetch(0);
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.ea = (short) (cpuState.getWP() + (mop.val << 1));
			if (mop.byteop)
				state.prev = state.value = (short) (cpuState.getConsole().readByte(state.ea) & 0xff);
			else
				state.prev = state.value = cpuState.getConsole().readWord(state.ea);
		}
		
		@Override
		protected short preExecuteEA() {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			return (short) (changeBlock.cpuState.getWP() + (mop.val << 1));
		}
	}
	
	public final static class CalculateCruOffset extends BaseOperandChangeElement {
		public CalculateCruOffset(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuState9900 cpuState_) {
			CpuState9900 cpuState = (CpuState9900) cpuState_;
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.prev = state.value = (short) ((cpuState.readWorkspaceRegister(12) >> 1) + mop.val);
		}
		

		@Override
		public String format(IOperand op, boolean preExecute) {
			if (op != state.mop)
				return null;
			
			short value;
			if (preExecute) {
				MachineOperand9900 mop = (MachineOperand9900) state.mop;
				value = (short) (changeBlock.cpuState.getRegister(12) + mop.val);
			} else {
				value = state.value;
			}
			return ">" + HexUtils.toHex4(value);
		}
	}
	
	public final static class CalculateShift extends BaseOperandChangeElement {
		public CalculateShift(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuState9900 cpuState) {
			changeBlock.counts.addFetch(8);

			state.value = (short) (cpuState.readWorkspaceRegister(0) & 0xf);
		    if (state.value == 0) {
		    	state.value = 16;
			}
		    state.prev = state.value;
		}
		
		@Override
		public String format(IOperand op, boolean preExecute) {
			if (preExecute) {
				short v = (short) (changeBlock.cpuState.getRegister(0) & 0xf);
			    if (v == 0) {
			    	v = 16;
				}
			    return Integer.toString(v);
			} else {
				return Integer.toString(state.value);
			}
				
		}
	}
	
	public final static class AdvancePC extends BaseChangeElement {
		public final int value;
		private int prev;

		public AdvancePC(ChangeBlock9900 changes, int value) {
			super(changes);
			this.value = value;
		}

		@Override
		public void apply(ICpuState cpuState) {
			prev = cpuState.getRegister(Cpu9900.REG_PC);
			cpuState.setRegister(Cpu9900.REG_PC, prev + value);
		}

		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(Cpu9900.REG_PC, prev);
		}
	}

	public final static class JumpPC extends BaseChangeElement implements IOperandChangeElement {
		public final int value;
		private int prev;
		private int cycles;

		public JumpPC(ChangeBlock9900 changes, int value, int cycles) {
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
		
		@Override
		public String format(IOperand op, boolean preExecute) {
			if (op != changeBlock.inst.getOp1())
				return null;
			
			return ">" + HexUtils.toHex4(changeBlock.inst.pc + changeBlock.inst.getSize() + value);
		}
	}



	public final static class ReadIndirectRegister extends BaseRegisterOperandChangeElement {
		public ReadIndirectRegister(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuState9900 cpuState) {
			changeBlock.counts.addFetch(4);
			
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			state.ea = (short) (cpuState.getWP() + (mop.val << 1));
			
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
		
		@Override
		protected short preExecuteEA() {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short ea = (short) (changeBlock.cpuState.getWP() + (mop.val << 1));
			
			// get register value
			ea = changeBlock.cpuState.getConsole().flatReadWord(state.ea);
			return ea;
		}
	}
	
	public final static class ReadIncrementRegister extends BaseRegisterOperandChangeElement {
		public ReadIncrementRegister(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuState9900 cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			
			changeBlock.counts.addFetch(4);
			changeBlock.counts.addFetch(mop.byteop ? 2 : 4);
			
			short regAddr = (short) (cpuState.getWP() + (mop.val << 1));
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
		@Override
		protected void doRevert(CpuState9900 cpuState) {
			super.doRevert(cpuState);
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short regAddr = (short) (cpuState.getWP() + (mop.val << 1));
			cpuState.getConsole().flatWriteWord(regAddr, state.prev);
		}
		
		@Override
		protected short preExecuteEA() {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short ea = (short) (changeBlock.cpuState.getWP() + (mop.val << 1));
			
			// get register value
			ea = changeBlock.cpuState.getConsole().flatReadWord(state.ea);
			return ea;
		}
	}
	
	public final static class ReadRegisterOffset extends BaseRegisterOperandChangeElement {
		public ReadRegisterOffset(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuState9900 cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			
    		changeBlock.counts.addFetch(8);
    		
			state.ea = mop.immed; 
    		if (mop.val != 0) {
    			int offs = cpuState.readWorkspaceRegister(mop.val);
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
		
		@Override
		protected short preExecuteEA() {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			short ea;
			ea = mop.immed; 
    		if (mop.val != 0) {
    			int offs = changeBlock.cpuState.getRegister(mop.val);
    			ea += offs;
    		}
			return ea;
		}

	}
	
	public static class SaveContext implements IChangeElement {
		protected short pc, wp, st;
		
//		@Override
//		public IChangeElement clone() {
//			try {
//				return (IChangeElement) super.clone();
//			} catch (CloneNotSupportedException e) {
//				assert false;
//				return null;
//			}
//		}

		@Override
		public final void apply(ICpuState cpuState) {
			doApply((CpuState9900) cpuState);
		}

		@Override
		public final void revert(ICpuState cpuState) {
			doRevert((CpuState9900) cpuState);
		}

		protected void doApply(CpuState9900 cpuState) {
			pc = cpuState.getPC();
			wp = cpuState.getWP();
			st = cpuState.getST();
		}

		protected void doRevert(CpuState9900 cpuState) {
			cpuState.setPC(pc);
			cpuState.setWP(wp);
			cpuState.setST(st);
		}

	}
	
	public static abstract class SwitchContext extends SaveContext {
		protected int r13, r14, r15;
		
		public SwitchContext() {
		}
		
		protected abstract short getVectorAddress();
		
		@Override
		protected void doApply(CpuState9900 cpuState) {
			super.doApply(cpuState);

			short vec = getVectorAddress();
			short newWP = cpuState.getConsole().readWord(vec);
			short newPC = cpuState.getConsole().readWord(vec + 2);

			r13 = cpuState.getConsole().readWord(newWP + 13*2);
			r14 = cpuState.getConsole().readWord(newWP + 14*2);
			r15 = cpuState.getConsole().readWord(newWP + 15*2);

			((CpuState9900) cpuState).contextSwitch(newWP, newPC);
			
		}
		
		@Override
		protected void doRevert(CpuState9900 cpuState) {
			cpuState.writeWorkspaceRegister(13, r13);
			cpuState.writeWorkspaceRegister(14, r14);
			cpuState.writeWorkspaceRegister(15, r15);
			super.doRevert(cpuState);
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
		@Override
		protected void doApply(CpuState9900 cpuState) {
			super.doApply(cpuState);
			cpuState.setST((short) 0);
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
		protected void doApply(CpuState9900 cpuState) {
			super.doApply(cpuState);
			r11 = cpuState.getRegister(11);	// no count
			cpuState.writeWorkspaceRegister(11, op.ea);
			cpuState.setST((short) (cpuState.getST() | Status9900.ST_X));
		}
		
		@Override
		public void doRevert(CpuState9900 cpuState) {
			cpuState.setRegister(11, r11);
			super.doRevert(cpuState);
		}
	}
	
	public static class RestoreContext extends SaveContext {
		@Override
		protected void doApply(CpuState9900 cpuState) {
			super.doApply(cpuState);
			cpuState.setPC((short) cpuState.readWorkspaceRegister(14));
			cpuState.setST((short) cpuState.readWorkspaceRegister(15));
			cpuState.setWP((short) cpuState.readWorkspaceRegister(13));
		}
	}
	
	public static final class WriteResult extends BaseOperandChangeElement {
		public WriteResult(ChangeBlock9900 changes, MachineOperandState state) {
			super(changes, state);
		}

		@Override
		protected void doApply(CpuState9900 cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
            if (mop.byteop) {
            	state.prev = cpuState.getConsole().flatReadByte(state.ea);
				cpuState.getConsole().writeByte(state.ea, (byte) state.value);
			} else {
				state.prev = cpuState.getConsole().flatReadWord(state.ea);
				cpuState.getConsole().writeWord(state.ea, state.value);
			}
		}
		
		@Override
		protected void doRevert(CpuState9900 cpuState) {
			MachineOperand9900 mop = (MachineOperand9900) state.mop;
            if (mop.byteop) {
				cpuState.getConsole().writeByte(state.ea, (byte) state.prev);
			} else {
				cpuState.getConsole().writeWord(state.ea, state.prev);
			}
            super.doRevert(cpuState);
		}
		
		@Override
		public final String format(IOperand op, boolean preExecute) {
			if (op != state.mop || preExecute)
				return null;

			MachineOperand9900 mop = (MachineOperand9900) state.mop;
			String str;
			if (mop.byteop)
				str = HexUtils.toHex2(state.value);
			else
				str = HexUtils.toHex4(state.value);
			return ">" +  str + " (@" + HexUtils.toHex4(state.ea) + ")";
		}
		
	}

	public static class Flush implements IChangeElement {
		public final ChangeBlock9900 changes;
		private ICycleCalculator cycleCalculator;

		public Flush(ChangeBlock9900 changes) {
			this.changes = changes;
			this.cycleCalculator = InstTable9900.instCycles.get(changes.inst.getInst());
		}

//		@Override
//		public IChangeElement clone() {
//			try {
//				return (IChangeElement) super.clone();
//			} catch (CloneNotSupportedException e) {
//				assert false;
//				return null;
//			}
//		}
		
		/* (non-Javadoc)
		 * @see v9t9.common.cpu.IChangeElement#apply(v9t9.common.cpu.ICpuState)
		 */
		@Override
		public void apply(ICpuState cpuState) {
			//if (cycleCalculator.addCycles(changes);
			changes.counts.addExecute(cycleCalculator.getExecuteCycles());

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