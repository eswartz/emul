/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.emulator.runtime.compiler.Compiler9900;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.Status;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu9900 extends CpuBase {
    public static final int TMS_9900_BASE_CYCLES_PER_SEC = 3000000;
	/* interrupt pins */
	public static final int INTLEVEL_RESET = 0;
	public static final int INTLEVEL_LOAD = 1;
	public static final int INTLEVEL_INTREQ = 2;
	
    public Cpu9900(Machine machine, int interruptTick, VdpHandler vdp) {
    	super(machine, new CpuState9900(machine.getConsole()), interruptTick, vdp);
    	
        settingCyclesPerSecond.setInt(TMS_9900_BASE_CYCLES_PER_SEC);

    }
    
    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.cpu.Cpu#getBaseCyclesPerSec()
     */
    @Override
    public int getBaseCyclesPerSec() {
    	return TMS_9900_BASE_CYCLES_PER_SEC;
    }

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
    	ic = forceIcTo1 ? 1 : level;
    }
    
    /** 
     * When set, implement TI-99/4A behavior where all interrupts
     * are perceived as level 1.
     */
    private boolean forceIcTo1 = true;
    
    public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_LOAD = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
    /** When intreq, the interrupt level (IC* bits on the TMS9900). */
    private byte ic;
	public static final int REG_PC = 16;
	public static final int REG_ST = 17;
	public static final int REG_WP = 18;
  

    /**
     * 
     */
    public void contextSwitch(short newwp, short newpc) {
    	//System.out.println("contextSwitch from " + 
    	//Utils.toHex4(WP)+"/"+Utils.toHex4(PC) +
    	//" to " + Utils.toHex4(newwp)+"/"+Utils.toHex4(newpc));
        short oldwp = ((CpuState9900) state).getWP();
        short oldpc = state.getPC();
        ((CpuState9900) state).setWP(newwp);
        state.setPC(newpc);
        state.getConsole().writeWord(newwp + 13 * 2, oldwp);
        state.getConsole().writeWord(newwp + 14 * 2, oldpc);
        state.getConsole().writeWord(newwp + 15 * 2, getST());
        noIntCount = 2;
   }

    public void contextSwitch(int addr) {
    	//idle = false;
        contextSwitch(state.getConsole().readWord(addr), state.getConsole().readWord(addr+2));
        if (addr == 0) {
            /*
             * this mimics the behavior where holding down fctn-quit keeps the
             * program going
             */
            // TODO
            //trigger9901int(M_INT_VDP);
            //holdpin(INTPIN_INTREQ);
        }
    }

    /**
     * Poll the TMS9901 to see if any interrupts are pending.
     * @return true if any pending
     */
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
	    		ic = forceIcTo1 ? 1 : cruAccess.getInterruptLevel(); 
	    		if (state.getStatus().getIntMask() >= ic) {
	    			//System.out.println("Triggering interrupt... "+ic);
	    			pins |= PIN_INTREQ;
	    			return true;    		
	    		} else {
	    			//System.out.print('-');
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_LOAD + PIN_RESET) != 0)) {
    		System.out.println("Pins set... " + Integer.toHexString(pins));
    		return true;
    	}   
    	
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#handleInterrupts()
	 */
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
    	if ((pins & PIN_LOAD) != 0) {
            // non-maskable
            
        	// this is ordinarily reset by external hardware, but
        	// we don't yet have a way to scan instruction execution
        	pins &= ~PIN_LOAD;
        	
            System.out.println("**** NMI ****");
            contextSwitch(0xfffc);
            
            addCycles(22);
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
            System.out.println("**** RESET ****");
            state.getStatus().expand((short) 0);
            contextSwitch(0);
            addCycles(26);
            
            pins = 0;
            ic = 0;
            
            // ensure the startup code has enough time to clear memory
            noIntCount = 10000;
            
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else if ((pins & PIN_INTREQ) != 0 && state.getStatus().getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=');
        	//interrupts++;
            contextSwitch(0x4 * ic);
            addCycles(22);
            
            // no more interrupt until 9901 gives us another
            ic = 0;
                
            // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
            machine.getExecutor().interpretOneInstruction();
        }
    }

	public void setCruAccess(CruAccess access) {
		this.cruAccess = access;
	}

	public CruAccess getCruAccess() {
		return cruAccess;
	}

	public void saveState(ISettingSection section) {
		super.saveState(section);
		section.put("PC", state.getPC());
		section.put("WP", ((CpuState9900) state).getWP());
		section.put("status", state.getStatus().flatten());
		section.put("ForceAllIntsLevel1", forceIcTo1);
	}

	public void loadState(ISettingSection section) {
		if (section == null) {
			setPin(INTLEVEL_RESET);
			return;
		}
		
		state.setPC((short) section.getInt("PC"));
		((CpuState9900) state).setWP((short) section.getInt("WP"));
		state.getStatus().expand((short) section.getInt("status"));
		forceIcTo1 = section.getBoolean("ForceAllIntsLevel1");
		super.loadState(section);
		
	}

	@Override
	public String getCurrentStateString() {
		return "WP=>" 
		+ HexUtils.toHex4(getWP())
		+ "\t\tST=" +getStatus();
	}

	@Override
	public void reset() {
		contextSwitch(0);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#nmi()
	 */
	@Override
	public void nmi() {
		setPin(Cpu9900.PIN_LOAD);		
	}
	
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return ((pc >= 0x6000 && pc < 0x8000) 
				&& Compiler9900.settingDumpModuleRomInstructions.getBoolean());
	}

	/**
	 * @return
	 */
	public int getWP() {
		return ((CpuState9900) state).getWP();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#createStatus()
	 */
	@Override
	public Status createStatus() {
		return state.createStatus();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getPC()
	 */
	@Override
	public short getPC() {
		return state.getPC();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		return state.getRegister(reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getST()
	 */
	@Override
	public short getST() {
		return state.getST();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setPC(short)
	 */
	@Override
	public void setPC(short pc) {
		state.setPC(pc);
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

	/**
	 * @param wp
	 */
	public void setWP(short wp) {
		((CpuState9900) state).setWP(wp);
	}
}