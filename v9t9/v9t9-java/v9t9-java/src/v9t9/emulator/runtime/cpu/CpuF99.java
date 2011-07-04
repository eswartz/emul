package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.StatusF99;

/**
 * The F99 engine.
 * 
 * @author ejs
 */
public class CpuF99 extends CpuBase {
    /**
	 * 
	 */
	public static final int INT_BASE = 0xffe0;
	public static final int BASE_CYCLES_PER_SEC = 500000;
	private CpuStateF99 statef99;
	
	public CpuF99(Machine machine, int interruptTick, VdpHandler vdp) {
		super(machine, new CpuStateF99(machine.getConsole()), interruptTick, vdp);
		statef99 = (CpuStateF99) state;
        settingCyclesPerSecond.setInt(BASE_CYCLES_PER_SEC);
    }
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#getBaseCyclesPerSec()
	 */
	@Override
	public int getBaseCyclesPerSec() {
		return BASE_CYCLES_PER_SEC;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(state.toString());
		sb.append(" [");
		int sp = statef99.getSP();
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp)));
		sb.append(' ');
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp + 2)));
		sb.append(' ');
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp + 4))); 
		sb.append(']');
		return  sb.toString();
	}
	
	
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getPC()
	 */
    @Override
	public short getPC() {
        return state.getPC();
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setPC(short)
	 */
    @Override
	public void setPC(short pc) {
       	state.setPC(pc);
    }

    public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_NMI = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
	public static final int INT_RESET = 15;
	public static final int INT_NMI = 14;
	public static final int INT_VDP = 1;
	public static final int INT_BKPT = 0;
    
    /** When intreq, the interrupt level */
    byte ic;
    
	public static final int SP = 0;
	public static final int RSP = 1;
	public static final int PC = 2;
	public static final int UP = 3;
	public static final int SR = 4;
	
	public static final int REG_COUNT = 5;
    
	 /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#resetInterruptRequest()
	 */
    public void resetInterruptRequest() {
    	pins &= ~PIN_INTREQ;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setInterruptRequest(byte)
	 */
    public void setInterruptRequest(byte level) {
    	pins |= PIN_INTREQ;
    }
    
	/**
     * Poll the TMS9901 to see if any interrupts are pending.
     * @return true if any pending
     */
    @Override
	public final boolean doCheckInterrupts() {
    	// do not allow interrupts after some instructions
	    if (noIntCount > 0) {
	    	noIntCount--;
	    	return false;
	    }
	    
	    vdp.syncVdpInterrupt(machine);
	    
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = cruAccess.getInterruptLevel(); 
	    		int mask = getStatus().getIntMask();
    			if (mask >= ic) {
	    			pins |= PIN_INTREQ;
	    			return true;    		
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_NMI + PIN_RESET) != 0)) {
    		System.out.println("Pins set... "+pins);
    		return true;
    	}   
    	
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#handleInterrupts()
	 */
    @Override
	public final void handleInterrupts() {
    	PrintWriter dumpfull = Executor.getDumpfull();
		if (dumpfull != null) {
    		dumpfull.println("*** Aborted");
		}
        PrintWriter dump = Executor.getDump();
		if (dump != null) {
        	dump.println("*** Aborted");
		}
        
    	// non-maskable
    	if ((pins & PIN_NMI) != 0) {
            // non-maskable
            
        	// this is ordinarily reset by external hardware, but
        	// we don't yet have a way to scan instruction execution
        	pins &= ~PIN_NMI;
        	
            idle = false;
            

            System.out.println("**** NMI ****");

            triggerInterrupt(INT_NMI);
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
            System.out.println("**** RESET ****");

            triggerInterrupt(INT_RESET);
            
            machine.getExecutor().interpretOneInstruction();
        } else if ((pins & PIN_INTREQ) != 0 && getStatus().getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=');

        	triggerInterrupt(ic);
            
            // no more interrupt until 9901 gives us another
            ic = 0;
                
            // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
            machine.getExecutor().interpretOneInstruction();
        }
    }

   
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
        return state.getRegister(reg);
    }

	public void setCruAccess(CruAccess access) {
		this.cruAccess = access;
	}

	public CruAccess getCruAccess() {
		return cruAccess;
	}

	@Override
	public void saveState(ISettingSection section) {
		section.put("SP", ((CpuStateF99)state).getSP());
		section.put("SP0", ((CpuStateF99)state).getBaseSP());
		section.put("RP", ((CpuStateF99)state).getRP());
		section.put("RP0", ((CpuStateF99)state).getBaseRP());
		section.put("UP", ((CpuStateF99)state).getUP());
		section.put("UP0", ((CpuStateF99)state).getBaseUP());
		section.put("ST", ((CpuStateF99)state).getST());
		
		super.saveState(section);
	}

	@Override
	public void loadState(ISettingSection section) {
		if (section == null) {
			//setPin(INTLEVEL_RESET);
			return;
		}
		
		statef99.setSP((short) section.getInt("SP"));
		statef99.setBaseSP((short) section.getInt("SP0"));
		statef99.setRP((short) section.getInt("RP"));
		statef99.setBaseRP((short) section.getInt("RP0"));
		statef99.setUP((short) section.getInt("UP"));
		statef99.setBaseUP((short) section.getInt("UP0"));
		state.setST((short) section.getInt("ST"));
		
		super.loadState(section);
	}

	@Override
	public Status createStatus() {
		return new StatusF99();
	}
	
	@Override
	public String getCurrentStateString() {
		return "SP=" + HexUtils.toHex4(state.getRegister(CpuF99.SP)) 
		+ "\t\tSR=" + getStatus().toString();
	}
	
	@Override
	public void reset() {
        getStatus().expand((short) 0);
        
        // ROM should set these!
		getState().setSP((short) 0xff80);
		getState().setRP((short) 0xffc0);
		getState().setUP((short) 0xff00);
		
		contextSwitch((short) 0x400);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#nmi()
	 */
	@Override
	public void nmi() {
		setPin(PIN_NMI);
	}

	public CpuStateF99 getState() {
		return (CpuStateF99) state;
	}
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getST()
	 */
	@Override
	public short getST() {
		return state.getST();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int val) {
		state.setRegister(reg, val);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setST(short)
	 */
	@Override
	public void setST(short st) {
		state.setST(st);
	}

	public void push(short val) {
		short newSp = (short) ((statef99.getSP() - 2) & 0xfffe);
		state.getConsole().writeWord(newSp, val);
		statef99.setSP(newSp);
		
		if (newSp == 0) {
			reset();
		}
	}
	
	public short peek() {
		return state.getConsole().readWord(statef99.getSP());
	}

	public short pop() {
		short val = statef99.getConsole().readWord(statef99.getSP());
		short newSp = (short) ((statef99.getSP() + 2) & 0xfffe);
		statef99.setSP(newSp);
		return val;
	}

	public void rpush(short val) {
		short newRp = (short) ((statef99.getRP() - 2) & 0xfffe);
		statef99.getConsole().writeWord(newRp, val);
		statef99.setRP(newRp);
		
		if (newRp == 0) {
			reset();
		}
	}

	public short rpeek() {
		return statef99.getConsole().readWord(statef99.getRP());
	}
	public short rpop() {
		short val = statef99.getConsole().readWord(statef99.getRP());
		short newRp = (short) ((statef99.getRP() + 2) & 0xfffe);
		statef99.setRP(newRp);
		return val;
	}

	/**
	 * @param intNmi
	 * @return
	 */
	private short getIntVecAddr(int intNum) {
		return (short) (INT_BASE + intNum * 2);
	}

	/**
	 * @param pc2
	 */
	public void contextSwitch(short vec) {
		rpush(getPC());
		short addr = machine.getConsole().readWord(vec);
		setPC(addr);
	}

	/**
	 * @param intr
	 */
	public void triggerInterrupt(int intr) {
		idle = false;
		rpush(((StatusF99)getStatus()).flatten());
		((StatusF99)getStatus()).setIntMask(0);
		short addr = getIntVecAddr(intr);
		contextSwitch(addr);
	}

	/**
	 * @return
	 */
	public int popd() {
		int hi = pop();
    	int val = (hi << 16) | (pop() & 0xffff);
		return val;
	}

	/**
	 * @param v
	 */
	public void pushd(int v) {
		push((short) (v & 0xffff));
		push((short) (v >> 16));
	}

	
}