/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.StatusMFP201;
import v9t9.engine.memory.MemoryDomain.MemoryAccessListener;

/**
 * The MFP201 engine.
 * 
 * @author ejs
 */
public class CpuMFP201 extends CpuBase {
    public static final int BASE_CYCLES_PER_SEC = 5000000;
	private final int SP = 13;
	private final int PC = 14;
	private final int SR = 15;

	private short[] regs = new short[16];
	
	public CpuMFP201(Machine machine, int interruptTick, VdpHandler vdp) {
		super(machine, interruptTick, vdp);
        settingCyclesPerSecond.setInt(BASE_CYCLES_PER_SEC);
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getPC()
	 */
    @Override
	public short getPC() {
        return regs[PC];
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setPC(short)
	 */
    @Override
	public void setPC(short pc) {
        regs[PC] = pc;
    }


    public StatusMFP201 getStatus() {
        return (StatusMFP201) status;
    }

    public void setStatus(StatusMFP201 status) {
        this.status = status;
    }

    public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_LOAD = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
    /** When intreq, the interrupt level (IC* bits on the TMS9900). */
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
	    if (!allowInts) {
	    	allowInts = true;
	    	return false;
	    }
	    
	    vdp.syncVdpInterrupt(machine);
	    
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = cruAccess.getInterruptLevel(); 
	    		if (status.getIntMask() >= ic) {
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
	 * @see v9t9.emulator.runtime.Cpu#checkInterrupts()
	 */
    @Override
	public final void checkInterrupts() {
    	if (doCheckInterrupts())
    		throw new AbortedException();
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#checkAndHandleInterrupts()
	 */
    @Override
	public final void checkAndHandleInterrupts() {
    	if (doCheckInterrupts())
    		handleInterrupts();
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
        	
            System.out.println("**** NMI ****");
            
            // TODO
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
            System.out.println("**** RESET ****");
            status.expand((short) 0);
            
            // TODO
            
            machine.getExecutor().interpretOneInstruction();
        } else if ((pins & PIN_INTREQ) != 0 && status.getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=');
        	interrupts++;
            //contextSwitch(0x4 * ic);
            
        	// TODO
            
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
        return regs[reg];
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
			section.put("R" + r, regs[r]);
		
		super.saveState(section);
	}

	@Override
	public void loadState(ISettingSection section) {
		if (section == null) {
			//setPin(INTLEVEL_RESET);
			return;
		}
		
		for (int r = 0; r < 16; r++)
			regs[r] = (short) section.getInt("R" + r);
		status.expand((short) regs[SR]);
		settingRealTime.loadState(section);
		settingCyclesPerSecond.loadState(section);
		
		super.loadState(section);
	}

	@Override
	public Status createStatus() {
		return new StatusMFP201();
	}
	
	@Override
	public String getCurrentStateString() {
		return "ST=" + HexUtils.toHex4(regs[PC]) + "\t\tSR=" + status.toString();
	}
	
	@Override
	public void reset() {
		// TODO
	}

	public short getStack() {
		return regs[SP];
	}
	
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return true;
	}
	
}