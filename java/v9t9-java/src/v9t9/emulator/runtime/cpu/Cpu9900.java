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
import v9t9.engine.cpu.Status9900;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu9900 extends CpuBase {
    public static final int TMS_9900_BASE_CYCLES_PER_SEC = 3000000;
	/** program counter */
	private short PC;
	/** workspace pointer */
	private short WP;
	/* interrupt pins */
	public static final int INTLEVEL_RESET = 0;
	public static final int INTLEVEL_LOAD = 1;
	public static final int INTLEVEL_INTREQ = 2;
	
	
    public Cpu9900(Machine machine, int interruptTick, VdpHandler vdp) {
    	super(machine, interruptTick, vdp);

        settingCyclesPerSecond.setInt(TMS_9900_BASE_CYCLES_PER_SEC);

    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getPC()
	 */
    public short getPC() {
        return PC;
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setPC(short)
	 */
    public void setPC(short pc) {
        PC = pc;
    }

    public Status9900 getStatus() {
        return (Status9900) status;
    }

    public void setStatus(Status9900 status) {
        this.status = status;
    }

    public short getWP() {
        return WP;
    }

    public void setWP(short wp) {
        // TODO: verify
        WP = wp;
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
  

    /**
     * 
     */
    public void contextSwitch(short newwp, short newpc) {
    	//System.out.println("contextSwitch from " + 
    	//Utils.toHex4(WP)+"/"+Utils.toHex4(PC) +
    	//" to " + Utils.toHex4(newwp)+"/"+Utils.toHex4(newpc));
        short oldwp = WP;
        short oldpc = PC;
        setWP(newwp);
        setPC(newpc);
        console.writeWord(newwp + 13 * 2, oldwp);
        console.writeWord(newwp + 14 * 2, oldpc);
        console.writeWord(newwp + 15 * 2, getST());
        allowInts = false;
   }

    public void contextSwitch(int addr) {
        contextSwitch(console.readWord(addr), console.readWord(addr+2));
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
	    if (!allowInts) {
	    	allowInts = true;
	    	return false;
	    }
	    
	    vdp.syncVdpInterrupt(machine);
	    
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = forceIcTo1 ? 1 : cruAccess.getInterruptLevel(); 
	    		if (status.getIntMask() >= ic) {
	    			//System.out.println("Triggering interrupt... "+ic);
	    			pins |= PIN_INTREQ;
	    			return true;    		
	    		} else {
	    			//System.out.print('-');
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
    public final void checkInterrupts() {
    	if (doCheckInterrupts())
    		throw new AbortedException();
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
            status.expand((short) 0);
            contextSwitch(0);
            addCycles(26);
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else if ((pins & PIN_INTREQ) != 0 && status.getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=');
        	interrupts++;
            contextSwitch(0x4 * ic);
            addCycles(22);
            
            // no more interrupt until 9901 gives us another
            ic = 0;
                
            // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
            machine.getExecutor().interpretOneInstruction();
        }
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#checkAndHandleInterrupts()
	 */
    public final void checkAndHandleInterrupts() {
    	if (doCheckInterrupts())
    		handleInterrupts();
    }
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getRegister(int)
	 */
	public int getRegister(int reg) {
        return console.readWord(WP + reg*2);
    }

	public void setCruAccess(CruAccess access) {
		this.cruAccess = access;
	}

	public CruAccess getCruAccess() {
		return cruAccess;
	}

	public void saveState(ISettingSection section) {
		super.saveState(section);
		section.put("PC", PC);
		section.put("WP", WP);
		section.put("status", status.flatten());
		section.put("ForceAllIntsLevel1", forceIcTo1);
	}

	public void loadState(ISettingSection section) {
		if (section == null) {
			setPin(INTLEVEL_RESET);
			return;
		}
		
		PC = (short) section.getInt("PC");
		WP = (short) section.getInt("WP");
		status.expand((short) section.getInt("status"));
		forceIcTo1 = section.getBoolean("ForceAllIntsLevel1");
		super.loadState(section);
		
	}

	@Override
	public Status createStatus() {
		return new Status9900();
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
	
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return ((pc >= 0x6000 && pc < 0x8000) 
				&& Compiler9900.settingDumpModuleRomInstructions.getBoolean());
	}
}