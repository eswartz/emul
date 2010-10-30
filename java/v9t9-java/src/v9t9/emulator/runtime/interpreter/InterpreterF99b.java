package v9t9.emulator.runtime.interpreter;

import java.util.BitSet;
import java.util.TreeMap;

import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuF99b;
import v9t9.emulator.runtime.cpu.CpuStateF99b;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.InstF99b;
import static v9t9.engine.cpu.InstF99b.*;
import v9t9.engine.cpu.InstructionF99b;
import v9t9.engine.cpu.InstructionWorkBlockF99b;
import v9t9.engine.cpu.MachineOperandF99b;
import v9t9.engine.cpu.StatusF99b;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryDomain.MemoryWriteListener;

/**
 * This class interprets F99b instructions one by one.
 * 
 * @author ejs
 */
public class InterpreterF99b implements Interpreter {
	Machine machine;

    MemoryDomain memory;
    
    InstructionWorkBlockF99b iblock;

	private CpuF99b cpu;

	private TreeMap<Integer, InstructionF99b> cachedInstrs = new TreeMap<Integer, InstructionF99b>();
	private MemoryWriteListener memoryListener;

	private BitSet instrMap;
	
    public InterpreterF99b(Machine machine) {
        this.machine = machine;
        this.cpu = (CpuF99b) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        iblock = new InstructionWorkBlockF99b(cpu);
        iblock.domain = memory;
        iblock.showSymbol = true;
        
        instrMap = new BitSet();
        
        memoryListener = new MemoryWriteListener() {
			
			@Override
			public void changed(MemoryEntry entry, int addr, boolean isByte) {
				invalidateInstructionCache(addr);
			}
		};
		memory.addWriteListener(memoryListener);
     }

    public void dispose() {
    	cachedInstrs.clear();
    	instrMap.clear();
    	memory.removeWriteListener(memoryListener);
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#execute(java.lang.Short)
	 */
    public void execute() {
    	InstructionListener[] instructionListeners = machine.getExecutor().getInstructionListeners();
    	executeAndListen(instructionListeners);
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
	 */
	@Override
	public void executeChunk(int numinsts, Executor executor) {
		for (int i = 0; i < numinsts; i++) {
			execute();
			executor.nInstructions++;
			cpu.checkAndHandleInterrupts();
			if (executor.interruptExecution)
				break;
		}
	}

	private void executeAndListen(InstructionListener[] instructionListeners) {
		iblock.pc = cpu.getPC();
		iblock.cycles = 0;
		iblock.st = cpu.getST();
		
		
	    InstructionF99b ins = getInstruction();
	    cpu.setPC(iblock.pc);
	    
	    executeAndListen(instructionListeners, ins);
	}

	/**
	 * @param instructionListeners
	 * @param ins
	 */
	private boolean executeAndListen(InstructionListener[] instructionListeners,
			InstructionF99b ins) {
        iblock.cycles = cpu.getCurrentCycleCount();

        iblock.pc = cpu.getPC();
        iblock.st = cpu.getST();
        iblock.sp = ((CpuStateF99b)cpu.getState()).getSP();
        iblock.rp = ((CpuStateF99b)cpu.getState()).getRP();
        iblock.up = ((CpuStateF99b)cpu.getState()).getUP();
        iblock.inst = ins;
        
        InstructionWorkBlockF99b block = null;
        
        if (instructionListeners != null) {
	        Pair<Integer, Integer> fx = InstF99b.getStackEffects(ins.getInst());
			if (fx != null) {
				int spused = fx.first;
				if (spused < 0)
					spused = 4;
				for (int i = 0; i < spused; i++)
					iblock.inStack[i] = iblock.getStackEntry(spused - i - 1);
			}
	        fx = InstF99b.getReturnStackEffects(ins.getInst());
			if (fx != null) {
				int rpused = fx.first;
				if (rpused < 0)
					rpused = 4;
				for (int i = 0; i < rpused; i++)
					iblock.inReturnStack[i] = iblock.getReturnStackEntry(rpused - i - 1);
			}
			
			block = new InstructionWorkBlockF99b(cpu);
	        this.iblock.copyTo(block);
        }
        
        /* execute */
        boolean jumped = interpret(ins);

    	/* notify listeners */
    	if (instructionListeners != null) {
	        iblock.pc = cpu.getPC();
	        iblock.st = cpu.getST();
	        iblock.sp = ((CpuStateF99b)cpu.getState()).getSP();
	        iblock.rp = ((CpuStateF99b)cpu.getState()).getRP();
	        iblock.up = ((CpuStateF99b)cpu.getState()).getUP();
        
        	block.cycles = cpu.getCurrentCycleCount();
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(block, iblock);
        	}
        	
        	iblock.showSymbol = (ins.getInst() == Iexit || ins.getInst() == Iexiti || ins.getInst() == Icall);
        }
	        
        return jumped;		
	}
	

	private boolean doInvalidateInstructionCache(int addr) {
		if (cachedInstrs.isEmpty())
			return true;

		InstructionF99b cached = cachedInstrs.remove(addr);
		if (cached != null) {
			int maxAddr = cached.pc + cached.getSize();
			while (addr++ < maxAddr)
				cachedInstrs.remove(addr);

			refreshCache();
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void refreshCache() {
		if (cachedInstrs.isEmpty()) {
			instrMap.clear();
			return;
		}
	}

	private void invalidateInstructionCache(int addr) {
		if (cachedInstrs.isEmpty())
			return;
		
		int first = instrMap.nextSetBit(addr);
		if (first < 0 || first >= addr + 6)
			return;
		
		int cnt = 6;
		while (cnt > 0 && !doInvalidateInstructionCache(first))
			first++;
	}

	private InstructionF99b getInstruction() {
		int pc = cpu.getPC() & 0xffff;
		InstructionF99b inst = cachedInstrs.get(pc);
		if (inst == null) {
			inst = parseInstructions((short) pc);
			cachedInstrs.put(pc, inst);
			instrMap.set(pc, pc + inst.getSize());
			//System.out.println(inst+": " + inst.getSize());
			refreshCache();
		} else {
			iblock.pc = (short) (pc + inst.getSize());
		}
		return inst;
	}

	/**
	 * @return
	 */
	private InstructionF99b parseInstructions(short pc) {
		
		short thisPc = pc;
		
		short opword = (short) memory.readByte(thisPc);
		if (opword < 0) {
			opword = (short) ((opword << 8) | (memory.readByte(thisPc + 1) & 0xff));
			iblock.pc += 2;
			// call
			return getCallInstruction(opword);
		}
		
		iblock.pc = (short) (pc + 1);
		
		if (opword == Iext || opword == Idouble) {
			opword = (short) (((opword << 8) | (memory.readByte(thisPc + 1) & 0xff)) & 0xffff);
			++iblock.pc;
		}
		
		InstructionF99b inst;
		
		inst = getInstruction(thisPc, opword);
		
		return inst;
	}

	private InstructionF99b getInstruction(short origPC, short opword) {
		int opcode = opword & 0xffff;
		
		InstructionF99b inst = new InstructionF99b();
		inst.pc = origPC;
		
		inst.opcode = opcode;
		inst.setInst(opcode);

		if (opcode >= IbranchX && opcode < I0branchX + 16) {
			inst.setInst(opcode & 0xf0);
			int val = (byte)(opcode<<4) >> 4;
			if (val < 0)
				val --;
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					val, 
					MachineOperandF99b.OP_ENC_IMM4));
		}
		else if (opcode >= IlitX && opcode < IlitX + 16) {
			inst.setInst(IlitX);
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					(byte)(opcode<<4) >> 4,
					MachineOperandF99b.OP_ENC_IMM4));
		}
		else if (opcode >= IlitX_d && opcode < IlitX_d + 16) {
			inst.setInst(IlitX_d);
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					(byte)(opcode<<4) >> 4,
							MachineOperandF99b.OP_ENC_IMM4));
		}
		else
			switch (opcode) {
			case IbranchB:
			case I0branchB: {
				int val = (byte) iblock.nextByte();
				if (val < 0)
					val -= 2;
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						val, MachineOperandF99b.OP_ENC_IMM8));
				break;
			}
			case IlitB:
			case IlitB_d:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						(byte) iblock.nextByte(), MachineOperandF99b.OP_ENC_IMM8));
				break;
			case ItoContext:
			case IcontextFrom:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						iblock.nextByte() & 0xff, MachineOperandF99b.OP_ENC_CTX));
				break;
			case Irpidx:
			case Ispidx:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						iblock.nextByte() & 0xff, MachineOperandF99b.OP_ENC_IMM8));
				break;
			case IlitW:
			case I0branchW:
			case IbranchW:  {
				int val = (short) iblock.nextWord();
				if (val < 0)
					val -= inst.getSize();
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						val, MachineOperandF99b.OP_ENC_IMM16));
				break;
			}
			case IlitD_d: {
				int lo = iblock.nextWord() & 0xffff;
				int hi = iblock.nextWord() & 0xffff;
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						lo | (hi << 16),
						MachineOperandF99b.OP_ENC_IMM32));
				break;
			}
			default:
				// no immediate	
			}
		
		inst.setSize(iblock.pc - inst.pc);
		
		return inst;
	}

	private InstructionF99b getCallInstruction(short op) {
		InstructionF99b inst = new InstructionF99b();
		inst.pc = cpu.getPC();
		inst.opcode = op;
		inst.setOp1(MachineOperandF99b.createImmediateOperand((short) (op << 1), MachineOperandF99b.OP_ENC_IMM15S1));
		inst.setInst(Icall);
		inst.setSize(2);
		return inst;
	}

	/**
     * Execute an instruction
     * @param ins
     */
    private boolean interpret(InstructionF99b ins) {
    	MachineOperandF99b mop1 = (MachineOperandF99b)ins.getOp1();
		cpu.addCycles(1);
		
		int fromPC = iblock.pc;
        
		switch (ins.getInst()) {
		case Icmp:
		case Icmp+1:
		case Icmp+2:
		case Icmp+3:
		case Icmp+4:
		case Icmp+5:
		case Icmp+6:
		case Icmp+7: {
        	short r = cpu.pop();
        	short l = cpu.pop();
        	boolean c = false;
        	switch (ins.getInst() & 0x7) {
        	case InstF99b.CMP_GE: c = l >= r; break;
        	case InstF99b.CMP_GT: c = l > r; break;
        	case InstF99b.CMP_LE: c = l <= r; break;
        	case InstF99b.CMP_LT: c = l < r; break;
        	case InstF99b.CMP_UGE: c = (l & 0xffff) >= (r & 0xffff); break;
        	case InstF99b.CMP_UGT: c = (l & 0xffff) >  (r & 0xffff); break;
        	case InstF99b.CMP_ULE: c = (l & 0xffff) <= (r & 0xffff); break;
        	case InstF99b.CMP_ULT: c = (l & 0xffff) <  (r & 0xffff); break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	return false;
		}
		case Icmp_d:
		case Icmp_d+1:
		case Icmp_d+2:
		case Icmp_d+3:
		case Icmp_d+4:
		case Icmp_d+5:
		case Icmp_d+6:
		case Icmp_d+7: {
        	int r = cpu.popd();
        	int l = cpu.popd();
        	boolean c = false;
        	switch (ins.getInst() & 0x7) {
        	case InstF99b.CMP_GE: c = l >= r; break;
        	case InstF99b.CMP_GT: c = l > r; break;
        	case InstF99b.CMP_LE: c = l <= r; break;
        	case InstF99b.CMP_LT: c = l < r; break;
        	case InstF99b.CMP_UGE: c = (((long)l) & 0xffffffffL) >= (((long)r) & 0xffffffffL); break;
        	case InstF99b.CMP_UGT: c = (((long)l) & 0xffffffffL) >  (((long)r) & 0xffffffffL); break;
        	case InstF99b.CMP_ULE: c = (((long)l) & 0xffffffffL) <= (((long)r) & 0xffffffffL); break;
        	case InstF99b.CMP_ULT: c = (((long)l) & 0xffffffffL) <  (((long)r) & 0xffffffffL); break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	return false;
        	}
		case Imath_start:
		case Imath_start+1:
		case Imath_start+2:
		case Imath_start+3:
		case Imath_start+4:
		case Imath_start+5: {
        	short r = cpu.pop();
        	short l = cpu.pop();
        	cpu.push((short) binOp(l, r, ins.getInst() - Imath_start));
        	return false;
		}
		case Idmath_start:
		case Idmath_start+1:
		case Idmath_start+2:
		case Idmath_start+3:
		case Idmath_start+4:
		case Idmath_start+5: {
        	int r = cpu.popd();
        	int l = cpu.popd();
        	cpu.pushd(binOp_d(l, r, ins.getInst() - Idmath_start));
        	return false;
		}
		case Imath_start+8:
		case Imath_start+9:
		case Imath_start+10:
		case Imath_start+11:
		case Imath_start+12:
		case Imath_start+13:
		case Imath_start+14:
		case Imath_start+15: {
        	short v = cpu.pop();
        	cpu.push((short) unaryOp(v, ins.getInst() - Imath_start));
        	return false;
		}
		case Idmath_start+8:
		case Idmath_start+9:
		case Idmath_start+10:
		case Idmath_start+11:
		case Idmath_start+12:
		case Idmath_start+13:
		case Idmath_start+14:
		case Idmath_start+15: {
        	int v = cpu.popd();
        	cpu.pushd(unaryOp_d(v, ins.getInst() - Idmath_start));
        	return false;
		}	
        case Iload:
        	cpu.push(memory.readWord(cpu.pop()));
        	break;
        case Icload:
        	cpu.push((short) (memory.readByte(cpu.pop()) & 0xff));
        	break;
        case Iload_d: {
        	int addr = cpu.pop();
        	cpu.push(memory.readWord(addr + 2));
        	cpu.push(memory.readWord(addr));
        	break;
        }
        case Istore: {
        	int addr = cpu.pop();
        	memory.writeWord(addr, cpu.pop());
        	break;
        }
        case Icstore: {
        	int addr = cpu.pop();
        	memory.writeByte(addr, (byte) cpu.pop());
        	break;
        }
        case Istore_d: {
        	int addr = cpu.pop();
        	memory.writeWord(addr, cpu.pop());
        	memory.writeWord(addr + 2, cpu.pop());
        	break;
        }
        	
        case IplusStore: {
        	short addr = cpu.pop();
        	iblock.domain.writeWord(addr, (short) (iblock.domain.readWord(addr) + cpu.pop()));
        	break;
        }
        case IplusStore_d: {
        	short addr = cpu.pop();
        	int add = cpu.popd();
        	int val = (iblock.domain.readWord(addr) << 16) | (iblock.domain.readWord(addr + 2) & 0xffff);
        	val += add;
        	iblock.domain.writeWord(addr, (short) (val >> 16));
        	iblock.domain.writeWord(addr + 2, (short) (val & 0xffff));
        	break;
        }
        	
        case IlitB:
        case IlitW:
        case IlitX:
        	cpu.push(mop1.immed);
        	break;
        case IlitX_d:
        case IlitB_d:
        case IlitD_d:
        	cpu.pushd(mop1.val);
        	break;
        	
        case Iexit:
        	cpu.setPC(cpu.rpop());
        	return true;
        case Iexiti:
        	cpu.setPC(cpu.rpop());
        	cpu.setST(cpu.rpop());
        	cpu.noIntCount+=2;
        	return true;
        	
        case Idup:
        	cpu.push(cpu.peek());
        	break;
        case Iqdup: {
        	short val = cpu.peek();
        	if (val != 0)
        		cpu.push(val);
        	break;
        }
        case I0branchX: 
        case I0branchB: 
        case I0branchW: 
        {
        	short targ = (short) (fromPC + mop1.immed);
        	if (cpu.pop() == 0) {
        		cpu.setPC(targ);
        		return true;
        	}
        	break;
        }
        case IbranchX: 
        case IbranchB: 
        case IbranchW: 
        {
        	short targ = (short) (fromPC + mop1.immed);
        	cpu.setPC(targ);
        	return true;
        }
        case I0equ:
        	cpu.push((short) (cpu.pop() == 0 ? -1 : 0));
        	break;
        case I0equ_d:
        	cpu.push((short) (cpu.popd() == 0 ? -1 : 0));
        	break;
        case Iequ:
        	cpu.push((short) (cpu.pop() == cpu.pop() ? -1 : 0));
        	break;
        case Iequ_d:
        	cpu.push((short) (cpu.popd() == cpu.popd() ? -1 : 0));
        	break;
        case Idrop:
        	cpu.pop();
        	break;
        case Iswap: {
        	short x = cpu.pop();
        	short y = cpu.pop();
        	cpu.push(x);
        	cpu.push(y);
        	break;
        }
        case Iover:
        	cpu.push(iblock.getStackEntry(1));
        	break;
        case Irot: {
        	short x = cpu.pop();
        	short y = cpu.pop();
        	short z = cpu.pop();
        	cpu.push(y);
        	cpu.push(x);
        	cpu.push(z);
        	break;
        }
        case Idup_d: {
        	int v = cpu.popd();
        	cpu.pushd(v);
        	cpu.pushd(v);
        	break;
        }
        
        case Iumul: {
        	int mul = (cpu.pop() & 0xffff) * (cpu.pop() & 0xffff);
        	cpu.push((short) (mul & 0xffff));
        	cpu.push((short) (mul >> 16));
        	break;
        }
        case Iudivmod: {
        	int div = cpu.pop() & 0xffff;
        	int num = cpu.popd();
        	if (div == 0) {
            	cpu.push((short) -1);
            	cpu.push((short) 0);
        	} else {
        		int quot = num / div;
        		int rem = num % div;
        		if (quot >= 65536) {
        			cpu.push((short) -1);
                	cpu.push((short) 0);
        		} else {
        			cpu.push((short) rem);
                	cpu.push((short) quot);
        		}
        	}
        	break;
        }
		case Iand:
			cpu.push((short) (cpu.pop() & cpu.pop()));
			break;
		case Ior:
			cpu.push((short) (cpu.pop() | cpu.pop()));
			break;
		case Ixor:
			cpu.push((short) (cpu.pop() ^ cpu.pop()));
			break;
		case Inot:
			cpu.push((short) (cpu.pop() != 0 ? 0 : -1));
			break;
		case Iand_d:
			cpu.pushd((cpu.popd() & cpu.popd()));
			break;
		case Ior_d:
			cpu.pushd((cpu.popd() | cpu.popd()));
			break;
		case Ixor_d:
			cpu.pushd((cpu.popd() ^ cpu.popd()));
			break;
		case Inot_d:
			cpu.pushd((cpu.popd() != 0 ? 0 : -1));
			break;

        case Icall:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(mop1.immed);
        	return true;
        case Iexecute:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(cpu.pop());
        	return true;
        	
        case ItoR:
        	cpu.rpush(cpu.pop());
        	break;
        case ItoR_d:
        	cpu.rpush(iblock.getStackEntry(1));
        	cpu.rpush(iblock.getStackEntry(0));
        	cpu.pop();
        	cpu.pop();
        	break;
        case IRfrom:
        	cpu.push(cpu.rpop());
        	break;
        case IRfrom_d:
        	cpu.push(iblock.getReturnStackEntry(1));
        	cpu.push(iblock.getReturnStackEntry(0));
        	break;
        case Irdrop:
        	cpu.rpop();
        	break;
        case Irdrop_d:
        	cpu.rpop();
        	cpu.rpop();
        	break;
        case IatR:
        	cpu.push(cpu.rpeek());
        	break;
        case Ispidx:
        	cpu.push(iblock.getStackEntry(mop1.immed));
        	break;
        case Irpidx:
        	cpu.push(iblock.getReturnStackEntry(mop1.immed));
        	break;
        	
        case Iuser:
        	cpu.push((short) (iblock.up + (cpu.pop() * 2)));
        	break;
        	
        case IloopUp: {
        	short next = (short) (iblock.getReturnStackEntry(0) + 1);
        	short lim = iblock.getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		cpu.push((short) (next != lim ? 0 : -1));
        	break;
        }
        case IplusLoopUp: {
        	short change = cpu.pop();
        	short cur = iblock.getReturnStackEntry(0);
        	short next = (short) (cur + change);
        	short lim = iblock.getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		cpu.push((short) ((lim != 0 ? next < lim : next >= change) ? 0 : -1));
        	break;
        }
        case IuplusLoopUp: {
        	short change = cpu.pop();
        	short cur = iblock.getReturnStackEntry(0);
        	short next = (short) (cur + change);
        	short lim = iblock.getReturnStackEntry(1);
        	cpu.rpop();
        	cpu.rpush(next);
        	cpu.push((short) ((lim != 0 ? (next & 0xffff) < (lim & 0xffff) 
        			: (next & 0xffff) >= (change & 0xffff)) ? 0 : -1));
        	break;
        }
        case IcontextFrom:
        	switch (mop1.immed) {
        	case CTX_SP:
        		cpu.push(((CpuStateF99b)cpu.getState()).getSP());
        		break;
        	case CTX_SP0:
        		cpu.push(((CpuStateF99b)cpu.getState()).getBaseSP());
        		break;
        	case CTX_RP:
        		cpu.push(((CpuStateF99b)cpu.getState()).getRP());
        		break;
        	case CTX_RP0:
        		cpu.push(((CpuStateF99b)cpu.getState()).getBaseRP());
        		break;
        	case CTX_UP:
        		cpu.push(((CpuStateF99b)cpu.getState()).getUP());
        		break;
        	case CTX_PC:
        		cpu.push(cpu.getPC());
        		break;
        	case CTX_INT:
        		cpu.push((short) cpu.getStatus().getIntMask());
        		break;
    		default:
    			cpu.push((short) -1);
    			break;
        	}
        	break;
        	
        case ItoContext:
        	switch (mop1.immed) {
        	case CTX_SP:
        		((CpuStateF99b)cpu.getState()).setSP(cpu.pop());
        		break;
        	case CTX_SP0:
        		((CpuStateF99b)cpu.getState()).setBaseSP(cpu.pop());
        		break;
        	case CTX_RP:
        		((CpuStateF99b)cpu.getState()).setRP(cpu.pop());
        		break;
        	case CTX_RP0:
        		((CpuStateF99b)cpu.getState()).setBaseRP(cpu.pop());
        		break;
        	case CTX_UP:
        		((CpuStateF99b)cpu.getState()).setUP(cpu.pop());
        		break;
        	case CTX_PC:
        		((CpuStateF99b)cpu.getState()).setPC(cpu.pop());
        		return true;
        	case CTX_INT:
        		((StatusF99b) ((CpuStateF99b)cpu.getState()).getStatus()).setIntMask(cpu.pop());
        		break;
        	default:
        		cpu.pop();
        		break;
        	}
        	break;
        	
        default:
    		throw new UnsupportedOperationException("" + ins);
        }

		return false;
    }

	private int binOp(short l, short r, int immed) {
		switch (immed) {
		case OP_ADD:
			return l + r;
		case OP_SUB:
			return l - r;
		case OP_LSH:
			return l << ((r < 0) ? r & 0xf : r & 0x1f);
		case OP_RSH:
			return (l & 0xffff) >>> ((r < 0) ? r & 0xf : r & 0x1f);
		case OP_ASH:
			return l >> (r < 0 ? r & 0xf : r & 0x1f);
		case OP_CSH:
			if (r < 0) r &= 0xf;
			return ( ((l & 0xffff) >>> (r & 0xf))
					| ((l & 0xffff) << (16 - (r & 0xf))) ) ;
		}
		return 0;
	}
	private int binOp_d(int l, int r, int immed) {
		switch (immed) {
		case OP_ADD:
			return l + r;
		case OP_SUB:
			return l - r;
		case OP_LSH:
			return l << ((r < 0) ? r & 0xf : r & 0x3f);
		case OP_RSH:
			return (int) ((((long)l) & 0xffffffffL) >>> ((r < 0) ? r & 0xf : r & 0x3f));
		case OP_ASH:
			return l >> ((r < 0) ? r & 0xf : r & 0x3f);
		case OP_CSH:
			if (r < 0) r &= 0xf;
			return (int) ( ((((long) l) & 0xffffffffL) >>> (r & 0x1f))
					| (((long) l) & 0xffffffffL) << (32 - (r & 0x1f)) );
		}
		return 0;
	}


	private int unaryOp(short v, int immed) {
		switch (immed) {
		case OP_1MINUS:
			return v-1;
		case OP_2MINUS:
			return v-2;
		case OP_1PLUS:
			return v+1;
		case OP_2PLUS:
			return v+2;
		case OP_NEG:
			return -v;
		case OP_INV:
			return ~v;
		case OP_2TIMES:
			return v<<1;
		case OP_2DIV:
			return v>>1;
		}
		return 0;
	}
	private int unaryOp_d(int v, int immed) {
		switch (immed) {
		case OP_1MINUS:
			return v-1;
		case OP_2MINUS:
			return v-2;
		case OP_1PLUS:
			return v+1;
		case OP_2PLUS:
			return v+2;
		case OP_NEG:
			return -v;
		case OP_INV:
			return ~v;
		case OP_2TIMES:
			return v<<1;
		case OP_2DIV:
			return v>>1;
		}
		return 0;
	}

	/**
	 * 
	 */
	public void setShowSymbol() {
		iblock.showSymbol = true;
	}
}