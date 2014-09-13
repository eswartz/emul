/*
  Interpreter9900.java

  (c) 2005-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.interpreter;

import static java.util.Collections.newSetFromMap;

import java.util.Map;
import java.util.WeakHashMap;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstInfo;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.IInterpreter;
import v9t9.common.cpu.IStatus;
import v9t9.common.cpu.MachineOperandState;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.hardware.ICruChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.hardware.ICruHandler;
import v9t9.machine.ti99.cpu.ChangeBlock9900;
import v9t9.machine.ti99.cpu.Changes;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator;
import v9t9.machine.ti99.cpu.Instruction9900;
import v9t9.machine.ti99.cpu.InstructionWorkBlock9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;
import v9t9.machine.ti99.cpu.Status9900;
import v9t9.machine.ti99.machine.TI99Machine;
import ejs.base.utils.ListenerList;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class NewInterpreter9900 implements IInterpreter {

	final IMachine machine;
	final ICruHandler cruHandler;
	final IDsrManager dsrManager;
	
    IMemoryDomain memory;

    // per-PC prebuilt instructions
    Map<IMemoryArea, Instruction9900[]> parsedInstructions; 
    //Instruction[] instructions; 
    
    InstructionWorkBlock9900 iblock;

	private Cpu9900 cpu;

	private Status9900 status;

	private CycleCounts cycleCounts;

    public NewInterpreter9900(IMachine machine) {
        this.machine = machine;
        if (machine instanceof TI99Machine) {
        	cruHandler = ((TI99Machine) machine).getCruManager();
        	dsrManager = ((TI99Machine) machine).getDsrManager();
        } else {
        	cruHandler = null;
        	dsrManager = null;
        }
        this.cpu = (Cpu9900) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new WeakHashMap<IMemoryArea, Instruction9900[]>();
        iblock = new InstructionWorkBlock9900(cpu.getState());
        status = (Status9900) cpu.getState().createStatus();
        cycleCounts = cpu.getCycleCounts();
     }

    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.interpreter.Interpreter#dispose()
     */
    @Override
    public void dispose() {
    	
    }
    
    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
     */
    public void executeChunk(int numinsts, IExecutor executor) {
    	// pretend the realtime and instructionListeners settings don't change often
    	//execute(numinsts);
    	Object[] listeners = executor.getInstructionListeners().toArray();
    	while (numinsts-- > 0) {
    		ChangeBlock9900 changes = new ChangeBlock9900(cpu);
    		changes.generate();
    		
			for (Object listener : listeners) {
    			if (!((IInstructionListener) listener).preExecute(iblock)) {
    				throw new AbortedException();
    			}
//    			if (!((IInstructionListener) listener).preExecute(changes)) {
//    				throw new AbortedException();
//    			}
    		}	
    		
    		changes.apply(cpu);

    		for (Object listener : listeners) {
    			((IInstructionListener) listener).executed(changes);
    		}	

	    	if (executor.breakAfterExecution(1) || cpu.isIdle()) 
				break;
    	}

    }
    
    /* (non-Javadoc)
     * @see v9t9.common.cpu.IInterpreter#execute(int)
     */
    @Override
    public void execute(int maxCycles) {
    }

	/* (non-Javadoc)
     * @see v9t9.engine.interpreter.IInterpreter#reset()
     */
    @Override
    public void reset() {
    	parsedInstructions.clear();
    }
    
    public static abstract class BaseInterpret implements IChangeElement {
    	protected final Instruction9900 inst;
    	protected short prevST;
    	protected final MachineOperandState mos1;
    	protected final MachineOperandState mos2;
		protected final MachineOperandState mos3;

		public BaseInterpret(Instruction9900 inst, MachineOperandState mos1, 
				MachineOperandState mos2, MachineOperandState mos3) {
			this.inst = inst;
			this.mos1 = mos1;
			this.mos2 = mos2;
			this.mos3 = mos3;
		}
		
		public BaseInterpret(Instruction9900 inst, MachineOperandState mos1, 
				MachineOperandState mos2) {
			this(inst, mos1, mos2, null);
		}
		public BaseInterpret(Instruction9900 inst, MachineOperandState mos1) {
			this(inst, mos1, null, null);
		}
		public BaseInterpret(Instruction9900 inst) {
			this(inst, null, null, null);
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + inst.toString();
		}
		
		protected abstract void doApply(CpuState9900 cpuState, Status9900 status);
		protected void doRevert(CpuState9900 cpuState, Status9900 status) {
			
		}
		
		@Override
		public final void apply(ICpuState cpuState_) {
			CpuState9900 cpuState = ((CpuState9900) cpuState_);
			Status9900 status = cpuState.getStatus();
			
			prevST = cpuState.getStatus().get();		// raw value
			
			doApply(cpuState, status);
		}

		@Override
		public final void revert(ICpuState cpuState_) {
			CpuState9900 cpuState = (CpuState9900) cpuState_;
			cpuState.getStatus().expand(prevST);
			doRevert(cpuState, cpuState.getStatus());
		}

	}

    public static abstract class BaseJump implements IChangeElement {
    	protected Instruction9900 inst;
    	protected MachineOperandState mos1;
		private short prevPC;
    	
    	public BaseJump(Instruction9900 inst, MachineOperandState mos1) {
    		this.inst = inst;
    		this.mos1 = mos1;
    	}
    	
    	@Override
    	public String toString() {
    		return getClass().getSimpleName() + ": " + inst.toString();
    	}
    	
    	protected abstract boolean test(Status9900 status);
    	
    	@Override
    	public final void apply(ICpuState cpuState_) {
    		CpuState9900 cpuState = ((CpuState9900) cpuState_);
    		Status9900 status = cpuState.getStatus();

    		prevPC = cpuState.getPC();

    		if (test(status)) {
    			cpuState.setPC((short) (inst.pc + mos1.value));
    		}
    	}
    	
    	@Override
    	public final void revert(ICpuState cpuState_) {
    		cpuState_.setPC(prevPC);
    	}
    	
    }
    
    public static void appendInterpret(final Cpu9900 cpu, final ChangeBlock9900 changes,
			Instruction9900 inst, MachineOperandState mos1,
			MachineOperandState mos2, MachineOperandState mos3) {
		
    	final ICruHandler cruHandler = cpu.getMachine() instanceof TI99Machine
    			? ((TI99Machine) cpu.getMachine()).getCruManager() : null;
    			
        switch (inst.getInst()) {
        case InstTableCommon.Idata:
            break;
        case Inst9900.Ili:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = mos2.value;
					status.set_LAE(mos1.value);
				}
			});
            break;
        case Inst9900.Iai:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_LAECO(mos1.value, mos2.value);
					mos1.value += mos2.value;
				}
			});
            break;
        case Inst9900.Iandi:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value &= mos2.value;
					status.set_LAE(mos1.value);
				}
			});
            break;
        case Inst9900.Iori:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value |= mos2.value;
					status.set_LAE(mos1.value);
				}
			});
            break;
        case Inst9900.Ici:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_CMP(mos1.value, mos2.value);
				}
			});
            break;
        case Inst9900.Istwp:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = cpuState.getWP();
				}
			});
            break;
        case Inst9900.Istst:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = cpuState.getStatus().get();
				}
			});
            break;
        case Inst9900.Ilwpi:
        	changes.push(new BaseInterpret(inst, mos1) {
        		short prevWP;
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					prevWP = cpuState.getWP();
					cpuState.setWP(mos1.value);
				}
				@Override
				protected void doRevert(CpuState9900 cpuState, Status9900 status) {
					super.doRevert(cpuState, status);
					cpuState.setWP(prevWP);
				}
			});
            break;
        case Inst9900.Ilimi:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					cpuState.getStatus().setIntMask(mos1.value);
				}
			});
            break;
        case Inst9900.Iidle:
        	changes.push(new BaseInterpret(inst) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					cpu.setIdle(true);
				}
				@Override
				protected void doRevert(CpuState9900 cpuState, Status9900 status) {
					super.doRevert(cpuState, status);
					cpu.setIdle(false);
				}
			});
        	break;
        case Inst9900.Irset:
            //cpu.rset(); // TODO
            break;
        case Inst9900.Irtwp:
			changes.push(new Changes.RestoreContext());
			break;
        case Inst9900.Ickon:
        	changes.push(new BaseInterpret(inst) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					// TODO
				}
			});
            break;
        case Inst9900.Ickof:
        	changes.push(new BaseInterpret(inst) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					// TODO
				}
			});
            break;
        case Inst9900.Ilrex:
        	changes.push(new BaseInterpret(inst) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					// TODO
				}
			});
            break;
        case Inst9900.Iblwp:
        	changes.push(new Changes.Blwp(mos1));
        	break;

        case Inst9900.Ib:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					cpuState.setPC(mos1.value);
				}
			});
            break;
        case Inst9900.Ix: {
        	// placeholder for replacement instruction, after this one
        	final int pos = changes.getCount() + 1;
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					ChangeBlock9900 newBlock = new ChangeBlock9900(changes.cpu, inst.pc, 
							mos1.value);
					newBlock.appendOperandFetch();
					newBlock.appendInstructionExecute();
					newBlock.appendFlush();
					changes.insert(pos, newBlock);
				}
			});
            break;
        }
        
        case Inst9900.Iclr:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = 0;
				}
			});
            break;
        case Inst9900.Ineg:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = (short) -mos1.value;
					status.set_LAEO(mos1.value);
				}
			});
            break;
        case Inst9900.Iinv:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = (short) ~mos1.value;
					status.set_LAE(mos1.value);
				}
			});
            break;
        case Inst9900.Iinc:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_LAECO(mos1.value, (short) 1);
					mos1.value ++;
				}
			});
            break;
        case Inst9900.Iinct:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_LAECO(mos1.value, (short) 2);
					mos1.value += 2;
				}
			});
            break;
        case Inst9900.Idec:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_LAECO(mos1.value, (short) -1);
					mos1.value --;
				}
			});
            break;
        case Inst9900.Idect:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_LAECO(mos1.value, (short) -2);
					mos1.value -= 2;
				}
			});
            break;
        case Inst9900.Ibl:
        	changes.push(new BaseInterpret(inst, mos1) {
        		int prevR11;
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					prevR11 = cpuState.getRegister(11);
					cpuState.setRegister(11, cpuState.getPC());
					cpuState.setPC(mos1.value);
				}
				@Override
				protected void doRevert(CpuState9900 cpuState, Status9900 status) {
					super.doRevert(cpuState, status);
					cpuState.setRegister(11, prevR11);
				}
			});
            break;
        case Inst9900.Iswpb:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = (short) (mos1.value >> 8 & 0xff | mos1.value << 8 & 0xff00);
				}
			});
            break;
        case Inst9900.Iseto:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos1.value = -1;
				}
			});
            break;
        case Inst9900.Iabs:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_LAEO(mos1.value);
					if ((mos1.value & 0x8000) != 0) {
						mos1.value = (short) -mos1.value;
					}
				}
			});
            break;
        case Inst9900.Isra:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_SHIFT_RIGHT_C(mos1.value, mos2.value);
					mos1.value = (short) (mos1.value >> mos2.value);
					status.set_LAE(mos1.value);
				}
			});
            break;
        case Inst9900.Isrl:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_SHIFT_RIGHT_C(mos1.value, mos2.value);
					mos1.value = (short) ((mos1.value & 0xffff) >> mos2.value);
					status.set_LAE(mos1.value);
				}
			});
            break;

        case Inst9900.Isla:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_SHIFT_LEFT_CO(mos1.value, mos2.value);
					mos1.value = (short) (mos1.value << mos2.value);
					status.set_LAE(mos1.value);
				}
			});
            break;

        case Inst9900.Isrc:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_SHIFT_RIGHT_C(mos1.value, mos2.value);
					mos1.value = (short) ((mos1.value & 0xffff) >> mos2.value | (mos1.value & 0xffff) << 16 - mos2.value);
					status.set_LAE(mos1.value);
				}
			});
            break;

        case Inst9900.Ijmp:
        	changes.push(new Changes.AdvancePC(mos1.value - 2));
            break;
        case Inst9900.Ijlt:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isLT();
        		}
        	});
        	break;
        case Inst9900.Ijle:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isLE();
        		}
        	});
        	break;

        case Inst9900.Ijeq:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isEQ();
        		}
        	});
            break;
        case Inst9900.Ijhe:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isHE();
        		}
        	});
            break;
        case Inst9900.Ijgt:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isGT();
        		}
        	});
            break;
        case Inst9900.Ijne:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isNE();
        		}
        	});
            break;
        case Inst9900.Ijnc:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return !status.isC();
        		}
        	});
            break;
        case Inst9900.Ijoc:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isC();
        		}
        	});
            break;
        case Inst9900.Ijno:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return !status.isO();
        		}
        	});
            break;
        case Inst9900.Ijl:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isL();
        		}
        	});
            break;
        case Inst9900.Ijh:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			return status.isH();
        		}
        	});
            break;

        case Inst9900.Ijop:
        	changes.push(new BaseJump(inst, mos1) {
        		protected boolean test(Status9900 status) {
        			// jump on ODD parity
        			return (status.isP());
        			
        		}
        	});
            break;

        case Inst9900.Isbo:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					if (cruHandler != null)
						cruHandler.writeBits(mos1.value<<1, 1, 1);
				}
			});
            break;

        case Inst9900.Isbz:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					if (cruHandler != null)
						cruHandler.writeBits(mos1.value<<1, 0, 1);
				}
			});
            break;

        case Inst9900.Itb:
        	changes.push(new BaseInterpret(inst, mos1) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					if (cruHandler != null) {
						mos1.value = (short) cruHandler.readBits(mos1.value<<1, 1);
					}
					status.set_E(mos1.value == 1);
				}
			});
            break;

        case Inst9900.Icoc:
          	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value = (short) (mos1.value & mos2.value);
					status.set_E(mos1.value == mos2.value);
				}
          	});
            break;

        case Inst9900.Iczc:
          	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value = (short) (mos1.value & ~mos2.value);
					status.set_E(mos1.value == mos2.value);
				}
          	});
            break;

        case Inst9900.Ixor:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value ^= mos1.value;
					status.set_LAE(mos2.value);
				}
          	});
            break;

        case Inst9900.Ixop:
			changes.push(new Changes.Xop(mos1, mos2));
            break;

        case Inst9900.Impy:
        	changes.push(new BaseInterpret(inst, mos1, mos2, mos3) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					int val = (mos1.value & 0xffff)
							* (mos2.value & 0xffff);
					mos3.value = (short) val;
					mos2.value = (short) (val >> 16);
				}
          	});

            break;

        case Inst9900.Idiv:
        	changes.push(new BaseInterpret(inst, mos1, mos2, mos3) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
		            if ((mos1.value & 0xffff) > (mos2.value & 0xffff)) {
		                int low = mos3.value & 0xffff;
		                long dval = ((mos2.value & 0xffff) << 16
		                        | (low & 0xffff)) & 0xffffffffL;
		                try {
		                    mos2.value = (short) (dval / (mos1.value & 0xffff));
		                    mos3.value = (short) (dval % (mos1.value & 0xffff));
		                    status.set_O(false);
		                } catch (ArithmeticException e) {
		                	status.set_O(true);
		                }
		            } else {
		            	status.set_O(true);
		            }
				}
        	});
            break;

        case Inst9900.Ildcr:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					if (cruHandler != null)
						cruHandler.writeBits(
								cpuState.getConsole().readWord(cpuState.getWP() + 12 * 2), mos1.value,
								mos2.value);
				}
			});
            break;

        case Inst9900.Istcr:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					if (cruHandler != null)
						mos1.value = (short) cruHandler.readBits(
								cpuState.getConsole().readWord(cpuState.getWP() + 12 * 2), mos2.value);
				}
			});
            break;
        case Inst9900.Iszc:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			mos2.value &= ~mos1.value;
        			status.set_LAE(mos2.value);
        		}
        	});
        	break;
        case Inst9900.Iszcb:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value &= ~mos1.value;
					status.set_BYTE_LAEP((byte) mos2.value);
				}
			});
            break;

        case Inst9900.Is:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			status.set_SUB_LAECO(mos2.value, mos1.value);
        			mos2.value -= mos1.value;
        		}
        	});
        	break;
        case Inst9900.Isb:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_SUB_BYTE_LAECOP((byte) mos2.value, (byte) mos1.value);
					mos2.value -= mos1.value;
				}
			});
            break;

        case Inst9900.Ic:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			status.set_CMP(mos1.value, mos2.value);
        		}
        	});
        	break;
        case Inst9900.Icb:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_BYTE_CMP((byte) mos1.value, (byte) mos2.value);
				}
			});
            break;

        case Inst9900.Ia:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			status.set_ADD_LAECO(mos2.value, mos1.value);
        			mos2.value += mos1.value;
        		}
        	});
        	break;
        case Inst9900.Iab:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					status.set_ADD_BYTE_LAECOP((byte) mos2.value, (byte) mos1.value);
					mos2.value += mos1.value;
				}
			});
            break;

        case Inst9900.Imov:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			mos2.value = mos1.value;
        			status.set_LAE(mos2.value);
        		}
        	});
        	break;
        case Inst9900.Imovb:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value = mos1.value;
					status.set_BYTE_LAEP((byte) mos2.value);
				}
			});
            break;

        case Inst9900.Isoc:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
        		@Override
        		protected void doApply(CpuState9900 cpuState, Status9900 status) {
        			mos2.value |= mos1.value;
        			status.set_LAE(mos2.value);
        		}
        	});
        	break;
        case Inst9900.Isocb:
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					mos2.value |= mos1.value;
					status.set_BYTE_LAEP((byte) mos2.value);
				}
			});
            break;

//        case InstTableCommon.Idsr:
//        	if (dsrManager != null)
//        		dsrManager.handleDSR(iblock);
//        	break;
//        	
        case InstTableCommon.Iticks: {
        	changes.push(new BaseInterpret(inst, mos1, mos2) {
				@Override
				protected void doApply(CpuState9900 cpuState, Status9900 status) {
					int count = cpu.getTickCount();
		        	mos1.value = (short) (count >> 16);
		        	mos2.value = (short) (count & 0xffff);
				}
			});
        	break;
        }
//        case InstTableCommon.Idbg:
//        	machine.getExecutor().debugCount(mos1.value == 0 ? 1 : -1);
//        	break;
        default:
            throw new IllegalStateException();
        }

	}
}