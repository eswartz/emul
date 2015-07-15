/**
 * 
 */
package v9t9.machine.f99b.cpu;

import ejs.base.utils.HexUtils;
import v9t9.common.asm.IOperand;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IOperandChangeElement;
import v9t9.common.cpu.MachineOperandState;

/**
 * @author ejs
 *
 */
public final class Changes {

	protected abstract static class BaseChangeElement implements IChangeElement {
		public final ChangeBlockF99b changeBlock;

		public BaseChangeElement(ChangeBlockF99b changeBlock) {
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
			return true;
		}
		
	}
	
	public final static class AdvancePC extends BaseChangeElement {
		public final int value;
		private int prev;

		public AdvancePC(ChangeBlockF99b changes, int value) {
			super(changes);
			this.value = value;
		}

		@Override
		public void apply(ICpuState cpuState) {
			prev = cpuState.getRegister(CpuF99b.REG_PC);
			cpuState.setRegister(CpuF99b.REG_PC, prev + value);
		}

		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(CpuF99b.REG_PC, prev);
		}
	}

	public final static class JumpPC extends BaseChangeElement implements IOperandChangeElement {
		public final int value;
		private int prev;
		private int cycles;

		public JumpPC(ChangeBlockF99b changes, int value, int cycles) {
			super(changes);
			this.value = value;
			this.cycles = cycles;
		}

		@Override
		public void apply(ICpuState cpuState) {
			prev = cpuState.getRegister(CpuF99b.REG_PC);
			cpuState.setRegister(CpuF99b.REG_PC, prev + value);
			changeBlock.counts.addExecute(cycles);
		}

		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(CpuF99b.REG_PC, prev);
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
		public ReadIndirectRegister(ChangeBlockF99b changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuStateF99b cpuState) {
			changeBlock.counts.addFetch(4);
			
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
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
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			short ea = (short) (changeBlock.cpuState.getWP() + (mop.val << 1));
			
			// get register value
			ea = changeBlock.cpuState.getConsole().flatReadWord(state.ea);
			return ea;
		}
	}
	
	public final static class ReadIncrementRegister extends BaseRegisterOperandChangeElement {
		public ReadIncrementRegister(ChangeBlockF99b changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuStateF99b cpuState) {
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			
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
		protected void doRevert(CpuStateF99b cpuState) {
			super.doRevert(cpuState);
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			short regAddr = (short) (cpuState.getWP() + (mop.val << 1));
			cpuState.getConsole().flatWriteWord(regAddr, state.prev);
		}
		
		@Override
		protected short preExecuteEA() {
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			short ea = (short) (changeBlock.cpuState.getWP() + (mop.val << 1));
			
			// get register value
			ea = changeBlock.cpuState.getConsole().flatReadWord(state.ea);
			return ea;
		}
	}
	
	public final static class ReadRegisterOffset extends BaseRegisterOperandChangeElement {
		public ReadRegisterOffset(ChangeBlockF99b changes, MachineOperandState state) {
			super(changes, state);
		}
	
		@Override
		protected void doApply(CpuStateF99b cpuState) {
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			
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
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
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
			doApply((CpuStateF99b) cpuState);
		}

		@Override
		public final void revert(ICpuState cpuState) {
			doRevert((CpuStateF99b) cpuState);
		}

		protected void doApply(CpuStateF99b cpuState) {
			pc = cpuState.getPC();
			wp = cpuState.getWP();
			st = cpuState.getST();
		}

		protected void doRevert(CpuStateF99b cpuState) {
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
		protected void doApply(CpuStateF99b cpuState) {
			super.doApply(cpuState);

			short vec = getVectorAddress();
			short newWP = cpuState.getConsole().readWord(vec);
			short newPC = cpuState.getConsole().readWord(vec + 2);

			r13 = cpuState.getConsole().readWord(newWP + 13*2);
			r14 = cpuState.getConsole().readWord(newWP + 14*2);
			r15 = cpuState.getConsole().readWord(newWP + 15*2);

			((CpuStateF99b) cpuState).contextSwitch(newWP, newPC);
			
		}
		
		@Override
		protected void doRevert(CpuStateF99b cpuState) {
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
		protected void doApply(CpuStateF99b cpuState) {
			super.doApply(cpuState);
			r11 = cpuState.getRegister(11);	// no count
			cpuState.writeWorkspaceRegister(11, op.ea);
			cpuState.setST((short) (cpuState.getST() | StatusF99b.ST_X));
		}
		
		@Override
		public void doRevert(CpuStateF99b cpuState) {
			cpuState.setRegister(11, r11);
			super.doRevert(cpuState);
		}
	}
	
	public static class RestoreContext extends SaveContext {
		@Override
		protected void doApply(CpuStateF99b cpuState) {
			super.doApply(cpuState);
			cpuState.setPC((short) cpuState.readWorkspaceRegister(14));
			cpuState.setST((short) cpuState.readWorkspaceRegister(15));
			cpuState.setWP((short) cpuState.readWorkspaceRegister(13));
		}
	}
	
	public static final class WriteResult extends BaseOperandChangeElement {
		public WriteResult(ChangeBlockF99b changes, MachineOperandState state) {
			super(changes, state);
		}

		@Override
		protected void doApply(CpuStateF99b cpuState) {
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
            if (mop.byteop) {
            	state.prev = cpuState.getConsole().flatReadByte(state.ea);
				cpuState.getConsole().writeByte(state.ea, (byte) state.value);
			} else {
				state.prev = cpuState.getConsole().flatReadWord(state.ea);
				cpuState.getConsole().writeWord(state.ea, state.value);
			}
		}
		
		@Override
		protected void doRevert(CpuStateF99b cpuState) {
			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
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

			MachineOperandF99b mop = (MachineOperandF99b) state.mop;
			String str;
			if (mop.byteop)
				str = HexUtils.toHex2(state.value);
			else
				str = HexUtils.toHex4(state.value);
			return ">" +  str + " (@" + HexUtils.toHex4(state.ea) + ")";
		}
		
	}

	public static class Flush implements IChangeElement {
		public final ChangeBlockF99b changes;
		private ICycleCalculator cycleCalculator;

		public Flush(ChangeBlockF99b changes) {
			this.changes = changes;
			this.cycleCalculator = InstTableF99b.instCycles.get(changes.inst.getInst());
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
			
			if (changes.inst.getInst() != InstF99b.Irtwp)
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