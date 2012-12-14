package v9t9.machine.f99b.interpreter;

import static v9t9.machine.f99b.asm.InstF99b.CTX_INT;
import static v9t9.machine.f99b.asm.InstF99b.CTX_LP;
import static v9t9.machine.f99b.asm.InstF99b.CTX_PC;
import static v9t9.machine.f99b.asm.InstF99b.CTX_RP;
import static v9t9.machine.f99b.asm.InstF99b.CTX_RP0;
import static v9t9.machine.f99b.asm.InstF99b.CTX_SP;
import static v9t9.machine.f99b.asm.InstF99b.CTX_SP0;
import static v9t9.machine.f99b.asm.InstF99b.CTX_SR;
import static v9t9.machine.f99b.asm.InstF99b.CTX_UP;
import static v9t9.machine.f99b.asm.InstF99b.I0branchB;
import static v9t9.machine.f99b.asm.InstF99b.I0branchW;
import static v9t9.machine.f99b.asm.InstF99b.I0branchX;
import static v9t9.machine.f99b.asm.InstF99b.I0equ;
import static v9t9.machine.f99b.asm.InstF99b.IRfrom;
import static v9t9.machine.f99b.asm.InstF99b.Iand;
import static v9t9.machine.f99b.asm.InstF99b.IatR;
import static v9t9.machine.f99b.asm.InstF99b.IbranchB;
import static v9t9.machine.f99b.asm.InstF99b.IbranchW;
import static v9t9.machine.f99b.asm.InstF99b.IbranchX;
import static v9t9.machine.f99b.asm.InstF99b.Icall;
import static v9t9.machine.f99b.asm.InstF99b.Iccompare;
import static v9t9.machine.f99b.asm.InstF99b.Icfill;
import static v9t9.machine.f99b.asm.InstF99b.Icload;
import static v9t9.machine.f99b.asm.InstF99b.Icmove;
import static v9t9.machine.f99b.asm.InstF99b.Icmp;
import static v9t9.machine.f99b.asm.InstF99b.IcontextFrom;
import static v9t9.machine.f99b.asm.InstF99b.IcplusStore;
import static v9t9.machine.f99b.asm.InstF99b.Icstore;
import static v9t9.machine.f99b.asm.InstF99b.Idmath_start;
import static v9t9.machine.f99b.asm.InstF99b.Idouble;
import static v9t9.machine.f99b.asm.InstF99b.Idovar;
import static v9t9.machine.f99b.asm.InstF99b.Idrop;
import static v9t9.machine.f99b.asm.InstF99b.Idup;
import static v9t9.machine.f99b.asm.InstF99b.Iequ;
import static v9t9.machine.f99b.asm.InstF99b.Iexecute;
import static v9t9.machine.f99b.asm.InstF99b.Iexit;
import static v9t9.machine.f99b.asm.InstF99b.Iexiti;
import static v9t9.machine.f99b.asm.InstF99b.Ifill;
import static v9t9.machine.f99b.asm.InstF99b.IfromLocals;
import static v9t9.machine.f99b.asm.InstF99b.Ilalloc;
import static v9t9.machine.f99b.asm.InstF99b.IlitB;
import static v9t9.machine.f99b.asm.InstF99b.IlitW;
import static v9t9.machine.f99b.asm.InstF99b.IlitX;
import static v9t9.machine.f99b.asm.InstF99b.Iload;
import static v9t9.machine.f99b.asm.InstF99b.Ilocal;
import static v9t9.machine.f99b.asm.InstF99b.IloopUp;
import static v9t9.machine.f99b.asm.InstF99b.Ilpidx;
import static v9t9.machine.f99b.asm.InstF99b.Imath_start;
import static v9t9.machine.f99b.asm.InstF99b.Inand;
import static v9t9.machine.f99b.asm.InstF99b.Inot;
import static v9t9.machine.f99b.asm.InstF99b.Ior;
import static v9t9.machine.f99b.asm.InstF99b.Iover;
import static v9t9.machine.f99b.asm.InstF99b.IplusLoopUp;
import static v9t9.machine.f99b.asm.InstF99b.IplusStore;
import static v9t9.machine.f99b.asm.InstF99b.Iqdup;
import static v9t9.machine.f99b.asm.InstF99b.Irdrop;
import static v9t9.machine.f99b.asm.InstF99b.Irot;
import static v9t9.machine.f99b.asm.InstF99b.Irpidx;
import static v9t9.machine.f99b.asm.InstF99b.Ispidx;
import static v9t9.machine.f99b.asm.InstF99b.Istore;
import static v9t9.machine.f99b.asm.InstF99b.Iswap;
import static v9t9.machine.f99b.asm.InstF99b.Isyscall;
import static v9t9.machine.f99b.asm.InstF99b.ItoContext;
import static v9t9.machine.f99b.asm.InstF99b.ItoLocals;
import static v9t9.machine.f99b.asm.InstF99b.ItoR;
import static v9t9.machine.f99b.asm.InstF99b.Iudivmod;
import static v9t9.machine.f99b.asm.InstF99b.IuloopUp;
import static v9t9.machine.f99b.asm.InstF99b.Iumul;
import static v9t9.machine.f99b.asm.InstF99b.Iupidx;
import static v9t9.machine.f99b.asm.InstF99b.IuplusLoopUp;
import static v9t9.machine.f99b.asm.InstF99b.Iuser;
import static v9t9.machine.f99b.asm.InstF99b.Ixor;
import static v9t9.machine.f99b.asm.InstF99b.OP_1MINUS;
import static v9t9.machine.f99b.asm.InstF99b.OP_1PLUS;
import static v9t9.machine.f99b.asm.InstF99b.OP_2DIV;
import static v9t9.machine.f99b.asm.InstF99b.OP_2MINUS;
import static v9t9.machine.f99b.asm.InstF99b.OP_2PLUS;
import static v9t9.machine.f99b.asm.InstF99b.OP_2TIMES;
import static v9t9.machine.f99b.asm.InstF99b.OP_ADD;
import static v9t9.machine.f99b.asm.InstF99b.OP_ASH;
import static v9t9.machine.f99b.asm.InstF99b.OP_CSH;
import static v9t9.machine.f99b.asm.InstF99b.OP_INV;
import static v9t9.machine.f99b.asm.InstF99b.OP_LSH;
import static v9t9.machine.f99b.asm.InstF99b.OP_NEG;
import static v9t9.machine.f99b.asm.InstF99b.OP_RSH;
import static v9t9.machine.f99b.asm.InstF99b.OP_SUB;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_DEBUG_OFF;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_DEBUG_ON;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_DECORATED_NUMBER;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_FIND;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_GFIND;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_IDLE;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_NUMBER;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_REGISTER_SYMBOL;
import static v9t9.machine.f99b.asm.InstF99b.SYSCALL_SPIN;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.Pair;


import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.engine.interpreter.IInterpreter;
import v9t9.machine.f99b.asm.InstF99b;
import v9t9.machine.f99b.asm.InstructionF99b;
import v9t9.machine.f99b.asm.InstructionWorkBlockF99b;
import v9t9.machine.f99b.asm.MachineOperandF99b;
import v9t9.machine.f99b.asm.StatusF99b;
import v9t9.machine.f99b.cpu.CpuF99b;
import v9t9.machine.f99b.cpu.CpuStateF99b;

/**
 * This class interprets F99b instructions one by one.
 * 
 * @author ejs
 */
public class InterpreterF99b implements IInterpreter {
	public static boolean DEBUG = false;
	
	IMachine machine;

    IMemoryDomain memory;
    
    InstructionWorkBlockF99b iblock;

	private CpuF99b cpu;
	private CpuStateF99b cpuState;

	//private TreeMap<Integer, InstructionF99b> cachedInstrs = new TreeMap<Integer, InstructionF99b>();
	private InstructionF99b[] cachedInstrs = new InstructionF99b[65536];
	private int cachedInstrCount;
	private IMemoryWriteListener memoryWriteListener;

	private BitSet instrMap;

	private IRawInstructionFactory instructionFactory;
	
    public InterpreterF99b(IMachine machine) {
        this.machine = machine;
        this.cpu = (CpuF99b) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        instructionFactory = machine.getInstructionFactory();
        cpuState = (CpuStateF99b)cpu.getState();
		iblock = new InstructionWorkBlockF99b(cpuState);
        iblock.showSymbol = true;
        
        instrMap = new BitSet();
        
        memoryWriteListener = new IMemoryWriteListener() {
			
			@Override
			public void changed(IMemoryEntry entry, int addr, Number value) {
				invalidateInstructionCache(addr);
			}
		};
		memory.addWriteListener(memoryWriteListener);
     }

	public void dispose() {
    	Arrays.fill(cachedInstrs, null);
    	cachedInstrCount = 0;
    	instrMap.clear();
    	memory.removeWriteListener(memoryWriteListener);
    }
	
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#execute(java.lang.Short)
	 */
    public final void execute() {
    	ListenerList<IInstructionListener> instructionListeners = machine.getExecutor().getInstructionListeners();
    	executeAndListen(instructionListeners);
    }

    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
	 */
	@Override
	public void executeChunk(int numinsts, IExecutor executor) {
		int i;
		for (i = numinsts; i >= 4; i -= 4) {
			execute();
			execute();
			execute();
			execute();
			if (executor.breakAfterExecution(4) || cpu.isIdle()) 
				return;
		}
		while (i-- > 0) {
			execute();
			if (executor.breakAfterExecution(1) || cpu.isIdle()) 
				break;
		}
	}


	public short getStackEntry(int i) {
		return iblock.domain.readWord(cpuState.getSP() + i * 2);
	}
	public short getReturnStackEntry(int i) {
		return iblock.domain.readWord(cpuState.getRP() + i * 2);
	}
	public short getLocalStackEntry(int i) {
		return iblock.domain.readWord(cpuState.getLP() - (i + 1) * 2);
	}

	private final void executeAndListen(ListenerList<IInstructionListener> instructionListeners) {
		iblock.pc = cpu.getPC();
		
		if ((iblock.pc & 0xffff) < 0x400) {
			cpu.fault();
			return;
		}
		
		iblock.cycles = 0;
		iblock.st = cpu.getST();
		
		// get next instruction and advance
	    InstructionF99b ins = getInstruction();
		iblock.cycles = cpu.getCurrentCycleCount();
		
		//iblock.pc = cpu.getPC();	// do below, since PC was advanced
		iblock.st = cpu.getST();
		iblock.sp = cpuState.getSP();
		iblock.rp = cpuState.getRP();
		iblock.up = cpuState.getUP();
		iblock.lp = cpuState.getLP();
		iblock.inst = ins;
		
		InstructionWorkBlockF99b block = null;
		
		if (!instructionListeners.isEmpty()) {
		    Pair<Integer, Integer> fx = InstF99b.getStackEffects(ins.getInst());
			if (fx != null) {
				int spused = fx.first;
				if (spused < 0)
					spused = 4;
				iblock.inStack = new short[spused];
				for (int i = 0; i < spused; i++)
					iblock.inStack[i] = getStackEntry(spused - i - 1);
			}
		    fx = InstF99b.getReturnStackEffects(ins.getInst());
			if (fx != null) {
				int rpused = fx.first;
				if (rpused < 0)
					rpused = 4;
				iblock.inReturnStack = new short[rpused];
				for (int i = 0; i < rpused; i++)
					iblock.inReturnStack[i] = getReturnStackEntry(rpused - i - 1);
			}
			
			block = new InstructionWorkBlockF99b(cpuState);

			for (Object listener : instructionListeners.toArray()) {
				if (!((IInstructionListener) listener).preExecute(iblock)) {
					throw new AbortedException();
				}
			}
			
			this.iblock.copyTo(block);
		}
		
		cpu.setPC(iblock.pc);
		iblock.pc = cpu.getPC();
		
		/* execute */
		interpret(ins);
		
		/* notify listeners */
		if (!instructionListeners.isEmpty()) {
		    iblock.pc = cpu.getPC();
		    iblock.st = cpu.getST();
		    iblock.sp = cpuState.getSP();
		    iblock.rp = cpuState.getRP();
		    iblock.up = cpuState.getUP();
		    iblock.lp = cpuState.getLP();
		
			iblock.cycles = cpu.getCurrentCycleCount();
			for (Object listener : instructionListeners.toArray()) {
				((IInstructionListener) listener).executed(block, iblock);
			}
			
			iblock.showSymbol = (ins.getInst() == Iexit || ins.getInst() == Iexiti 
					|| ins.getInst() == Icall || ins.getInst() == Iexecute);
		}
	}

	/**
	 * 
	 */
	private void refreshCache() {
		if (cachedInstrCount == 0) {
			instrMap.clear();
			return;
		}
	}

	/**
	 * When the byte at @addr changes, delete any instructions that
	 * use that byte.
	 * @param addr
	 */
	private void invalidateInstructionCache(int addr) {
		if (cachedInstrCount == 0)
			return;
		
		// if ROM, no worries
		if (!memory.getEntryAt(addr).hasWriteAccess())
			return;
		
		// first, see if the map shows that any known instruction
		// uses the byte
		if (!instrMap.get(addr))
			return;
		
		// find where it is.  Instructions can be up to 6 bytes
		// (DLIT = double+lit+4 bytes)
		int first = instrMap.nextSetBit(Math.max(0, addr - 5));
		if (first < 0 || first >= addr + 6)
			return;
		
		int cnt = 6;
		while (cnt-- > 0 && !doInvalidateInstructionCache(first & 0xffff, addr))
			first++;
	}

	private boolean doInvalidateInstructionCache(int addr, int target) {
		InstructionF99b cached = cachedInstrs[addr];
		if (cached != null) {
			int maxAddr = cached.pc + cached.getSize();
			if (addr <= target && maxAddr > target) {
				cachedInstrs[addr] = null;
				cachedInstrCount--;
				refreshCache();
				return true;
			}
		}
		return false;
	}


	private InstructionF99b getInstruction() {
		int pc = cpu.getPC() & 0xffff;
		InstructionF99b inst = cachedInstrs[pc];
		if (inst == null) {
			inst = (InstructionF99b) instructionFactory.decodeInstruction(pc, memory);
			cachedInstrs[pc] = inst;
			cachedInstrCount++;
			instrMap.set(pc, pc + inst.getSize());
			//System.out.println(HexUtils.toHex4(pc)+": " + inst+": " + inst.getSize());
			refreshCache();
		}
		iblock.pc = (short) (pc + inst.getSize());
		return inst;
	}

	/**
     * Execute an instruction
     * @param ins
     * @return true if jumped
     */
    private final boolean interpret(InstructionF99b ins) {
    	if (ins.getInst() < InstF99b.Iimm_start || ins.getInst() >= InstF99b.Iimm_start + 8)
    		cpu.addCycles(ins.getSize());
    	else
    		cpu.addCycles(1);
    	
		if (ins.getInst() < 256) {
			return interpretShort(ins);
		} else if ((ins.getInst() >> 8) ==  Idouble) {
			return interpretDouble(ins);
		} else {
			return interpretExt(ins);
		}
    }
    
    private final boolean interpretShort(InstructionF99b ins) {
    	int fromPC = iblock.pc;
    	MachineOperandF99b mop1 = (MachineOperandF99b)ins.getOp1();
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
        case Iload:
        	cpu.push(memory.readWord(cpu.pop()));
        	break;
        case Icload:
        	cpu.push((short) (memory.readByte(cpu.pop()) & 0xff));
        	break;
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
        case IplusStore: {
        	short addr = cpu.pop();
        	iblock.domain.writeWord(addr, (short) (iblock.domain.readWord(addr) + cpu.pop()));
        	break;
        }
        case IcplusStore: {
        	short addr = cpu.pop();
        	iblock.domain.writeByte(addr, (byte) (iblock.domain.readByte(addr) + cpu.pop()));
        	break;
        }
        

        case IlitB:
        case IlitW:
        case IlitX:
        	cpu.push(mop1.immed);
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
        case Iequ:
        	cpu.push((short) (cpu.pop() == cpu.pop() ? -1 : 0));
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
        	cpu.push(getStackEntry(1));
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
		case Inand: {
			short m = cpu.pop();
			short v = cpu.pop();
			cpu.push((short) (v & ~m));
			break;
		}

        case Icall:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(mop1.immed);
        	return true;
        case Iexecute:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(cpu.pop());
        	return true;
        case Idovar:
    	{
    		short next = cpu.rpop();
        	cpu.push(iblock.pc);
        	cpu.setPC(next);
        	return true;
    	}
        	
        case ItoR:
        	cpu.rpush(cpu.pop());
        	break;
        case IRfrom:
        	cpu.push(cpu.rpop());
        	break;
        case Irdrop:
        	cpu.rpop();
        	break;
        case IatR:
        	cpu.push(cpu.rpeek());
        	break;
        case Ispidx:
        	cpu.push(getStackEntry(mop1.immed));
        	break;
        case Irpidx:
        	cpu.push(getReturnStackEntry(mop1.immed));
        	break;
        	
        case Ilpidx:
        	cpu.push(getLocalStackEntry(mop1.immed));
        	break;
        case Ilocal:
        	cpu.push((short) (iblock.lp - (mop1.immed + 1) * 2));
        	break;
        	
        case Iupidx:
        	cpu.push((short) (iblock.up + (mop1.val & 0xff)));
        	break;
        case Iuser:
        	cpu.push((short) (iblock.up + cpu.pop()));
        	break;
        	
        case IloopUp: {
        	short next = (short) (getReturnStackEntry(0) + 1);
        	short lim = getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		cpu.push((short) ((lim != 0 ? next < lim : next >= 1) ? 0 : -1));
        	break;
        }
        case IuloopUp: {
        	int next = (getReturnStackEntry(0) + 1) & 0xffff;
        	short lim = getReturnStackEntry(1);
        	cpu.rpop();
        	cpu.rpush((short) next);
        	cpu.push((short) ((lim != 0 ? next < (lim & 0xffff) 
        			: next >= (1 & 0xffff)) ? 0 : -1));
        	break;
        }
        case IplusLoopUp: {
        	short change = cpu.pop();
        	short cur = getReturnStackEntry(0);
        	int next = (cur + change);
        	short lim = getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush((short) next);
    		cpu.push((short) ((lim != 0 ? (change < 0 ? next > lim  : next < lim)
    					: next >= change) ? 0 : -1));
        	break;
        }
        case IuplusLoopUp: {
        	short change = cpu.pop();
        	short cur = getReturnStackEntry(0);
        	int next = (cur & 0xffff) + change;
        	short lim = getReturnStackEntry(1);
        	cpu.rpop();
        	cpu.rpush((short) next);
        	cpu.push((short) ((lim != 0 ? (change < 0 ? next > (lim & 0xffff) : next < (lim & 0xffff)) 
        			: (next & 0xffff)  >= (change & 0xffff)) ? 0 : -1));
        	break;
        }
        
        case Icfill: {
        	cpu.addCycles(4);
        	int step = cpu.pop();
        	byte ch = (byte) cpu.pop();
        	int len = cpu.pop() & 0xffff;
        	int addr = cpu.pop();
        	while (len-- > 0) {
        		memory.writeByte(addr, ch);
        		addr += step;
        		cpu.addCycles(2);
        	}
        	break;
        }
        case Ifill: {
        	cpu.addCycles(3);
        	int step = cpu.pop();
        	short w = cpu.pop();
        	int len = cpu.pop() & 0xffff;
        	int addr = cpu.pop();
        	while (len-- > 0) {
        		memory.writeWord(addr, w);
        		addr += step*2;
        		cpu.addCycles(2);
        	}
        	break;
        }
        case Icmove: {
        	doCmove();
        	break;
        }
        case Iccompare: {
        	doCcompare();
        	break;
        }
        
        case Ilalloc: {
        	cpu.push((short) iblock.sp);
        	cpu.push((short) (iblock.rp - mop1.immed * 2));
        	cpu.push((short) (mop1.immed * 2));
        	cpu.push((short) -1);
        	cpu.push((short) -1);
        	doCmove();
        	cpuState.setSP((short) (iblock.sp + mop1.immed * 2));
        	cpuState.setRP((short) (iblock.rp - mop1.immed * 2));
        	break;
        }

        case IcontextFrom:
        	switch (mop1.immed) {
        	case CTX_INT:
        		cpu.push((short) cpuState.getStatus().getIntMask());
        		break;
        	case CTX_SP:
        		cpu.push(cpuState.getSP());
        		break;
        	case CTX_SP0:
        		cpu.push(cpuState.getBaseSP());
        		break;
        	case CTX_RP:
        		cpu.push(cpuState.getRP());
        		break;
        	case CTX_RP0:
        		cpu.push(cpuState.getBaseRP());
        		break;
        	case CTX_UP:
        		cpu.push(cpuState.getUP());
        		break;
        	case CTX_LP:
        		cpu.push(cpuState.getLP());
        		break;
        	case CTX_PC:
        		cpu.push(cpu.getPC());
        		break;
        	case CTX_SR:
        		cpu.push(cpuState.getStatus().flatten());
        		break;
    		default:
    			cpu.push((short) -1);
    			break;
        	}
        	break;
        	
        case ItoContext:
        	switch (mop1.immed) {
        	case CTX_INT:
        		((StatusF99b) cpuState.getStatus()).setIntMask(cpu.pop());
        		break;
        	case CTX_SP:
        		cpuState.setSP(cpu.pop());
        		break;
        	case CTX_SP0: {
        		short sp0 = cpu.pop();
        		cpuState.setBaseSP(sp0);
        		cpuState.setSP(sp0);
        		break;
        	}
        	case CTX_RP:
        		cpuState.setRP(cpu.pop());
        		break;
        	case CTX_RP0: {
        		short rp0 = cpu.pop();
        		cpuState.setBaseRP(rp0);
        		cpuState.setRP(rp0);
        		break;
        	}
        	case CTX_UP:
        		cpuState.setUP(cpu.pop());
        		break;
        	case CTX_LP:
        		cpuState.setLP(cpu.pop());
        		break;
        	case CTX_PC:
        		cpuState.setPC(cpu.pop());
        		return true;
        	case CTX_SR:
        		cpuState.setST(cpu.pop());
        		//((StatusF99b) cpuState.getStatus()).expand(cpu.pop());
        		break;
        	default:
        		cpu.pop();
        		break;
        	}
        	break;

        case Isyscall:
        	switch (mop1.immed) {
	        	case SYSCALL_DEBUG_OFF: {
	        		machine.getExecutor().debugCount(-1);
	            	break;
	        	}
	        	case SYSCALL_DEBUG_ON: {
	        		machine.getExecutor().debugCount(1);
	        		break;
	        	}
	        	
	        	case SYSCALL_IDLE: {
	        		machine.getCpu().setIdle(true);
	        		break;
	        	}
	        	case SYSCALL_REGISTER_SYMBOL: {
	        		int xt = cpu.pop();
	        		int nfa = cpu.pop();
	        		StringBuilder sb = new StringBuilder();
	        		int len = iblock.domain.readByte(nfa++) & 0x1f;
	        		while (len-- > 0) {
	        			char ch = (char) iblock.domain.readByte(nfa++);
	        			sb.append(ch);
	        		}
	        		iblock.domain.getEntryAt(xt).defineSymbol(xt, sb.toString());
	        		break;
	        	}
	        	
	        	case SYSCALL_FIND: {
	        		syscallFind();
	        		break;
	        	}

	        	case SYSCALL_GFIND: {
	        		syscallGfind();
	        		break;
	        	}

	        	case SYSCALL_NUMBER: {
	        		syscallNumber();
	        		break;
	        	}

	        	case SYSCALL_DECORATED_NUMBER: {
	        		syscallDecoratedNumber();
	        		break;
	        	}
	        	
	        	case SYSCALL_SPIN: {
	        		int toSpin = cpu.pop();
	        		if (cpu.getCurrentCycleCount() + toSpin > cpu.getTargetCycleCount()) {
	        			toSpin = cpu.getTargetCycleCount() - cpu.getCurrentCycleCount();
	        		}
	        		cpu.addCycles(toSpin);
	        		break;
	        	}
        	}
        	break;

        default:
        	unsupported(ins);
        }

		return false;
    }

	private void syscallFind() {
		// ( caddr lfa -- caddr 0 | xt -1=immed | xt 1 )
		int lfa = cpu.pop();
		int caddr = cpu.pop();

		boolean found = false;
		int[] after = { 0 }; 
		int count = 65536;
		while (lfa != 0 && count-- > 0) {
			cpu.addCycles(3);
			short nfa = (short) (lfa + 2);
			if (nameMatches(iblock.domain, caddr, nfa, after)) {
				short xt = (short) after[0];
				if ((xt & 1) != 0)
					xt++;
				cpu.push(xt);
				cpu.push((short) ((iblock.domain.readByte(nfa) & 0x40) != 0 ? 1 : -1));
				found = true;
				break;
			} else {
				lfa = iblock.domain.readWord(lfa);
			}
		}
		
		if (!found) {
			cpu.push((short) caddr);
			cpu.push((short) 0);
		}
	}

	private void syscallGfind() {
		// ( caddr gDictEnd gDict -- caddr 0 | xt 1 | xt -1 )
		short gromDictEnd = cpu.pop();
		short gromDict = cpu.pop();
		int caddr = cpu.pop();

		boolean found = false;
		short lastMatch = 0;
		int[] after = { 0 }; 
		
		if (DEBUG) {
			String name = readCountedString(iblock.domain, caddr);
			System.out.println("Searching for >>>"+name+"<<<");
		}
    		
		// we want to find the LAST entry with the name, but cannot search
		// backwards, because the compressed LFA-less nature of the dictionary
		// doesn't afford reliable backward scanning.
		IMemoryDomain grom = cpu.getMachine().getMemory().getDomain(IMemoryDomain.NAME_GRAPHICS);
		while (gromDict < gromDictEnd) {
			cpu.addCycles(3);
			if (nameMatches(grom, caddr, gromDict, after)) {
				lastMatch = gromDict;
				found = true;
			} 
			gromDict = (short) (after[0] + 2);
		}
		
		if (found) {
			byte descr = grom.readByte(lastMatch);
			cpu.push(grom.readWord(lastMatch + 1 + (descr & 0x1f)));
			cpu.push((short) (((descr & 0x40) != 0) ? 1 : -1));
		}
		else {
			cpu.push((short) caddr);
			cpu.push((short) 0);
		}
	}

	/**
	 * @param domain
	 * @param caddr
	 * @return
	 */
	private String readCountedString(IMemoryDomain domain, int caddr) {
		StringBuilder sb = new StringBuilder();
		int len = domain.readByte(caddr++) & 0x1f;
		while (len-- > 0) { 
			sb.append((char) domain.readByte(caddr++));
		}
		return sb.toString();
	}

	private void syscallNumber() {
		final String BASESTR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		// ( ud1 c-addr1 u1 base -- ud2 c-addr2 u2 )
		int base = cpu.pop();
		int len = cpu.pop();
		int caddr = cpu.pop();
		int val = cpu.popd();

		while (len > 0) {
			char ch = (char) iblock.domain.readByte(caddr);
			ch = Character.toUpperCase(ch);
			int v = BASESTR.indexOf(ch);
			if (v < 0 || v >= base) {
				break;
			}
			val = (val * base) + v;
			len--;
			caddr++;
			
			cpu.addCycles(10);
		}
		cpu.pushd(val);
		cpu.push((short) caddr);
		cpu.push((short) len);
	}

	private void syscallDecoratedNumber() {
		// ( addr u base -- ud dpl t | f )
		
		int base = cpu.pop();
		int len = cpu.pop();
		int caddr = cpu.pop();
		
		if (len > 0) {

			cpu.addCycles(50);
			
			int val = 0;
			boolean neg = false;
			boolean isDouble = false;

			char ch = (char) iblock.domain.readByte(caddr);
			if (ch == '-') {
				neg = true;
				caddr++;
				len--;
			}
			
			if (len > 0) {
				ch = (char) iblock.domain.readByte(caddr);
				if (ch == '$') {
					base = 16;
					caddr++;
					len--;
				}
				else if (ch == '&') {
					base =10;
					caddr++;
					len--;
				}
			}
			
			cpu.pushd(val);
			cpu.push((short) caddr);
			cpu.push((short) len);
			cpu.push((short) base);
			
			syscallNumber();
			
			len = cpu.pop();
			caddr = cpu.pop();
			
			if (len > 0) {
				if (iblock.domain.readByte(caddr) == '.') {
					
					isDouble = true;
					
					caddr++;
					len--;

					// NOTE: when floating point is supported, this is invalid
					//[[[ RFI 0004
					cpu.addCycles(10);
					cpu.push((short) caddr);
					cpu.push((short) len);
					cpu.push((short) base);

					syscallNumber();
					
					len = cpu.pop();
					caddr = cpu.pop();
					//]]]
				}
			}
			
			val = cpu.popd();

			cpu.pushd(neg ? -val : val);
			cpu.push((short) (isDouble ? -1 : 0));
			cpu.push((short) (len == 0 ? -1 : 0));
			return;
		} 
		
		cpu.push((short) 0);
	}
	
	/**
	 * @param caddr
	 * @param nfa
	 * @return
	 */
	private boolean nameMatches(IMemoryDomain domain, int caddr_, short nfa_, int[] after) {
		if (DEBUG) {
			String name = readCountedString(domain, nfa_);
			System.out.println("... >"+name+"< ?");
		}
		
		cpu.addCycles(10);
		int caddr = caddr_;
		short nfa = nfa_;
		byte clen = iblock.domain.readByte(caddr++);
		byte nlen = domain.readByte(nfa++);
		
		after[0] = nfa + (nlen & 0x1f);
		
		if (clen == 0 || (nlen & 0x80) == 0) /* hidden */
			return false;
		nlen &= 0x1f;
		if (clen != nlen)
			return false;
		
		cpu.addCycles(clen * 5);
		while (clen-- > 0) {
			char c = (char) iblock.domain.readByte(caddr++);
			char n = (char) domain.readByte(nfa++);
			if (Character.toLowerCase(c) != Character.toLowerCase(n))
				return false;
		}
		return true;
	}

	
	
	private void doCmove() {
		cpu.addCycles(2);
		int tstep = cpu.pop();
		int fstep = cpu.pop();
		int len = cpu.pop() & 0xffff;
		int taddr = cpu.pop();
		int faddr = cpu.pop();
		if (tstep < 0) {
			taddr -= tstep * (len - 1);
		}
		if (fstep < 0) {
			faddr -= fstep * (len - 1);
		}
		while (len-- > 0) {
			memory.writeByte(taddr & 0xffff, memory.readByte(faddr & 0xffff));
			faddr += fstep;
			taddr += tstep;
			cpu.addCycles(2);
		}
	}

	private void doCcompare() {
		cpu.addCycles(4);
		int tstep = cpu.pop();
		int fstep = cpu.pop();
		int len = cpu.pop() & 0xffff;
		int taddr = cpu.pop();
		int faddr = cpu.pop();
		if (tstep < 0) {
			taddr -= tstep * (len - 1);
		}
		if (fstep < 0) {
			faddr -= fstep * (len - 1);
		}
		int origfaddr = faddr;
		while (len-- > 0) {
			cpu.addCycles(4);
			byte src = memory.readByte(faddr & 0xffff);
			byte dst = memory.readByte(taddr & 0xffff);
			byte cmp = (byte) (src - dst);
			if (cmp != 0) {
				cpu.push((short) (faddr - origfaddr));
				cpu.push(cmp);
				return;
			}
			faddr += fstep;
			taddr += tstep;
		}
		cpu.push((short) (faddr - origfaddr));
		cpu.push((short) 0);
	}

    private final boolean interpretDouble(InstructionF99b ins) {
    	MachineOperandF99b mop1 = (MachineOperandF99b)ins.getOp1();
    	cpu.addCycles(1);
    	int baseInst = ins.getInst() & 0xff;
		switch (baseInst) {
		case Icmp:
		case Icmp+1:
		case Icmp+2:
		case Icmp+3:
		case Icmp+4:
		case Icmp+5:
		case Icmp+6:
		case Icmp+7: {
        	int r = cpu.popd();
        	int l = cpu.popd();
        	boolean c = false;
        	switch (baseInst & 0x7) {
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
        	int v = cpu.popd();
        	cpu.pushd(unaryOp_d(v, ins.getInst() - Idmath_start));
        	return false;
		}	
        case Iload: {
        	int addr = cpu.pop();
        	cpu.push(memory.readWord(addr));
        	cpu.push(memory.readWord(addr + 2));
        	break;
        }
        case Istore: {
        	int addr = cpu.pop();
        	memory.writeWord(addr + 2, cpu.pop());
        	memory.writeWord(addr, cpu.pop());
        	break;
        }
        	
        case IplusStore: {
        	short addr = cpu.pop();
        	int add = cpu.popd();
        	int val = (iblock.domain.readWord(addr + 2) << 16) | (iblock.domain.readWord(addr) & 0xffff);
        	val += add;
        	iblock.domain.writeWord(addr, (short) (val & 0xffff));
        	iblock.domain.writeWord(addr + 2, (short) (val >> 16));
        	break;
        }
        	
        case IlitX:
        case IlitB:
        case IlitW:	// really D
        	cpu.pushd(mop1.val);
        	break;
        case I0equ:
        	cpu.push((short) (cpu.popd() == 0 ? -1 : 0));
        	break;
        case Iequ:
        	cpu.push((short) (cpu.popd() == cpu.popd() ? -1 : 0));
        	break;
        case Idrop:
        	cpu.popd();
        	break;
        case Iswap: {
        	int x = cpu.popd();
        	int y = cpu.popd();
        	cpu.pushd(x);
        	cpu.pushd(y);
        	break;
        }
        case Idup: {
        	int v = cpu.popd();
        	cpu.pushd(v);
        	cpu.pushd(v);
        	break;
        }
        
        case Iudivmod: {
        	int div = cpu.popd() & 0xffffffff;
        	int numHi = cpu.popd();
        	int numLo = cpu.popd();
        	long num = (numHi << 32) | (numLo & 0xffffffff);
        	if (div == 0) {
            	cpu.push((short) -1);
            	cpu.push((short) 0);
        	} else {
        		long quot = num / div;
        		int rem = (int) (num % div);
        		if (quot >= 0x100000000L) {
        			cpu.pushd(-1);
                	cpu.pushd(0);
        		} else {
        			cpu.pushd(rem);
                	cpu.pushd((int) quot);
        		}
        	}
        	break;
        }
		case Iand:
			cpu.pushd((cpu.popd() & cpu.popd()));
			break;
		case Ior:
			cpu.pushd((cpu.popd() | cpu.popd()));
			break;
		case Ixor:
			cpu.pushd((cpu.popd() ^ cpu.popd()));
			break;
		case Inot:
			cpu.pushd((cpu.popd() != 0 ? 0 : -1));
			break;
		case Inand: {
			int v = cpu.popd();
			int m = cpu.popd();
			cpu.pushd(v & ~m);
			break;
		}
			
        case ItoR:
        	cpu.rpush(getStackEntry(1));
        	cpu.rpush(getStackEntry(0));
        	cpu.pop();
        	cpu.pop();
        	break;
        case IRfrom:
        	cpu.push(getReturnStackEntry(1));
        	cpu.push(getReturnStackEntry(0));
        	cpu.rpop();
        	cpu.rpop();
        	break;
        case Irdrop:
        	cpu.rpop();
        	cpu.rpop();
        	break;
        	
        case ItoLocals & 0xff: {
        	// LP@  	RP@ LP! ; \\ caller pushes R> 
        	short curLP = iblock.lp;
        	cpuState.setLP((short) (iblock.rp - 2));
        	cpu.rpush(curLP);
        	break;
        }
        case IfromLocals & 0xff: {
        	//  R>  LP@ RP!   R>  LP!  >R
        	cpuState.setRP((short) (iblock.lp));
        	short oldLP = cpu.rpop();
        	cpuState.setLP(oldLP);
        	break;
        }
        	
        default:
        	unsupported(ins);
        }

		return false;
    }

    private final boolean interpretExt(InstructionF99b ins) {
    	cpu.addCycles(1);
		switch (ins.getInst()) {
        default:
    		unsupported(ins);
		}
		return false;
    }
    
	/**
	 * @param ins
	 */
    private Set<Integer> unsupportedOpcodes = new HashSet<Integer>();
	private void unsupported(InstructionF99b ins) {
		if (!unsupportedOpcodes.contains(ins.getInst())) {
			unsupportedOpcodes.add(ins.getInst());
			System.out.println("Unsupported: " + HexUtils.toHex4(ins.getPc()) +" opcode = "+ ins.getInst() +": "+ ins);
		}
	}

	private final int binOp(short l, short r, int immed) {
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
	private final int binOp_d(int l, int r, int immed) {
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