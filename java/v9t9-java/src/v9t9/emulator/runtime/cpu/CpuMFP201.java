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
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.StatusMFP201;

/**
 * The MFP201 engine.
 * 
 * @author ejs
 */
public class CpuMFP201 extends CpuBase {
    public static final int BASE_CYCLES_PER_SEC = 500000;
	
	public CpuMFP201(Machine machine, int interruptTick, VdpHandler vdp) {
		super(machine, new CpuStateMFP201(machine.getConsole()), interruptTick, vdp);
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
		return state.toString();
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
    public static final int PIN_LOAD = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
	public static final int INT_RESET = 15;
	public static final int INT_NMI = 14;
	public static final int INT_BKPT = 0;
    
    /** When intreq, the interrupt level */
    byte ic;
    
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
	    		if (getStatus().getIntMask() >= ic) {
	    			pins |= PIN_INTREQ;
	    			return true;    		
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_LOAD + PIN_RESET) != 0)) {
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
    	if ((pins & PIN_LOAD) != 0) {
            // non-maskable
            
        	// this is ordinarily reset by external hardware, but
        	// we don't yet have a way to scan instruction execution
        	pins &= ~PIN_LOAD;
        	
            idle = false;
            

            System.out.println("**** NMI ****");
            
            // TODO
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
            System.out.println("**** RESET ****");
            getStatus().expand((short) 0);
            setPC(getConsole().readWord(0xfffe));

            idle = false;
            

            // TODO
            
            machine.getExecutor().interpretOneInstruction();
        } else if ((pins & PIN_INTREQ) != 0 && getStatus().getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=');
        	interrupts++;
            //contextSwitch(0x4 * ic);
            
        	// TODO
            
            idle = false;
            

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
		for (int r = 0; r < 16; r++)
			section.put("R" + r, state.getRegister(r));
		
		super.saveState(section);
	}

	@Override
	public void loadState(ISettingSection section) {
		if (section == null) {
			//setPin(INTLEVEL_RESET);
			return;
		}
		
		for (int r = 0; r < 16; r++)
			state.setRegister(r, section.getInt("R" + r));
		
		super.loadState(section);
	}

	@Override
	public Status createStatus() {
		return new StatusMFP201();
	}
	
	@Override
	public String getCurrentStateString() {
		return "SP=" + HexUtils.toHex4(state.getRegister(MachineOperandMFP201.SP)) 
		+ "\t\tSR=" + getStatus().toString();
	}
	
	@Override
	public void reset() {
		setPC(readIntVec(INT_RESET));
	}

	public short getSP() {
		return ((CpuStateMFP201) state).getSP();
	}
	public void setSP(short sp) {
		((CpuStateMFP201) state).setSP(sp);
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

	/**
	 * @param pc
	 */
	public void push(short val) {
		short newSp = (short) ((getSP() - 2) & 0xfffe);
		state.getConsole().writeWord(newSp, val);
		setSP(newSp);
		
		if (newSp == 0) {
			reset();
		}
	}

	/**
	 * @return
	 */
	public short pop() {
		short val = state.getConsole().readWord(getSP());
		short newSp = (short) ((getSP() + 2) & 0xfffe);
		setSP(newSp);
		return val;
	}

	/**
	 * @param intNmi
	 * @return
	 */
	public short readIntVec(int intNum) {
		return state.getConsole().readWord(0xffe0 + intNum * 2);
	}

	
}