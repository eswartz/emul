/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryDomain.MemoryAccessListener;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu implements MemoryAccessListener {
    Machine machine;
	public Memory memory;
	private MemoryDomain console;
	/** program counter */
	private short PC;
	/** workspace pointer */
	private short WP;
	long lastInterrupt;
	/* interrupt pins */
	//public static final int INTPIN_RESET = 1;
	//public static final int INTPIN_LOAD = 2;
	//public static final int INTPIN_INTREQ = 4;
	public static final int INTLEVEL_RESET = 0;
	public static final int INTLEVEL_LOAD = 1;
	public static final int INTLEVEL_INTREQ = 2;
	
	/*	Variables for controlling a "real time" emulation of the 9900
	processor.  Each call to execute() sets an estimated cycle count
	for the instruction and parameters in "instcycles".  We waste
	time in 1/BASE_EMULATOR_HZ second quanta to maintain the appearance of a
	3.0 MHz clock. */
	
	int         baseclockhz;
	
	int         instcycles;	// cycles for each instruction
	
	/** target # cycles to be executed per tick */
	int         targetcycles;	
	/** target # cycles to be executed for this tick */
	int         currenttargetcycles;	//
	/**  total # target cycles expected throughout execution */
	long         totaltargetcycles;	//
	/** current cycles per tick */
	int         currentcycles = 0;	// 
	/** total # current cycles executed */
	long         totalcurrentcycles;	// 
	private int interruptTick;	// # ms between CPU syncs
	private final VdpHandler vdp;

    public Cpu(Machine machine, int interruptTick, VdpHandler vdp) {
        this.machine = machine;
		this.vdp = vdp;
        this.memory = machine.getMemory();
        this.console = machine.getConsole();
        this.console.setAccessListener(this);
        this.status = new Status();
        this.interruptTick = interruptTick;
        
        settingCyclesPerSecond.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				baseclockhz = setting.getInt();
				targetcycles = (int)((long) baseclockhz * Cpu.this.interruptTick / 1000);
		        currenttargetcycles = targetcycles;
		        System.out.println("target: " + targetcycles);
			}
        	
        });
        
        settingCyclesPerSecond.setInt(3000000);

    }

    public short getPC() {
        return PC;
    }

    public void setPC(short pc) {
        PC = pc;
    }

    Status status;

    public short getST() {
        return status.flatten();
    }

    public void setST(short st) {
        status.expand(st);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public short getWP() {
        return WP;
    }

    public void setWP(short wp) {
        // TODO: verify
        WP = wp;
    }

    public void resetInterruptRequest() {
    	pins &= ~PIN_INTREQ;
    }
    
    /**
     * Called by the TMS9901 to indicate an interrupt is available.
     * @param level
     */
    public void setInterruptRequest(byte level) {
    	pins |= PIN_INTREQ;
    	ic = forceIcTo1 ? 1 : level;
    }
    
    /**
     * Called when hardware triggers another pin.
     */
    public void setPin(int mask) {
    	pins |= mask;
    }

    /** 
     * When set, implement TI-99/4A behavior where all interrupts
     * are perceived as level 1.
     */
    private boolean forceIcTo1 = true;
    
    public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_LOAD = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
    /** State of the pins above  */
    private int pins;
    /** When intreq, the interrupt level (IC* bits on the TMS9900). */
    private byte ic;
    
	private int ticks;
	private boolean allowInts;
	private CruAccess cruAccess;
	private int interrupts;

	static public final String sRealTime = "RealTime";

	static public final Setting settingRealTime = new Setting(
			sRealTime, new Boolean(false));

	static public final String sCyclesPerSecond = "CyclesPerSecond";

	static public final Setting settingCyclesPerSecond = new Setting(
			sCyclesPerSecond, new Integer(0));

    //public static Object executionToken;

    /**
     * @return
     */
    public Machine getMachine() {
        return machine;
    }

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
     */
    public void checkInterrupts() {
    	// do not allow interrupts after some instructions
	    if (!allowInts) {
	    	allowInts = true;
	    	return;
	    }
	    
	    vdp.syncVdpInterrupt();
	    
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = forceIcTo1 ? 1 : cruAccess.getInterruptLevel(); 
	    		if (status.getIntMask() >= ic) {
	    			//System.out.println("Triggering interrupt... "+ic);
	    			pins |= PIN_INTREQ;
	    			throw new AbortedException();	    		
	    		} else {
	    			//System.out.print('-');
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_LOAD + PIN_RESET) != 0)) {
    		System.out.println("Pins set... "+pins);
    		throw new AbortedException();
    	}   
    	
    	
    }
    
    /**
     * Called by toplevel in response to the AbortedException from above
     * (TODO: see if these still need to be distinct steps)
     */
    public void handleInterrupts() {
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

	public int getRegister(int reg) {
        return console.readWord(WP + reg*2);
    }

	public void setConsole(MemoryDomain console) {
		this.console = console;
	}

	public MemoryDomain getConsole() {
		return console;
	}

	public synchronized void addCycles(int cycles) {
		this.currentcycles += cycles; 
		
		vdp.addCpuCycles(cycles);
		//vdpInterruptFrac += cycles;
		//if (currentcycles > targetcycles)
		//	System.out.print('!');
	}

	public synchronized void tick() {
		//totaltargetcycles += targetcycles;
		totalcurrentcycles += currentcycles;
		
		// if we went over, aim for fewer this time
		currenttargetcycles = (int) (totaltargetcycles - totalcurrentcycles);
		if (currenttargetcycles > settingCyclesPerSecond.getInt() * 2) {
			// something really threw us off -- just start over
			totalcurrentcycles = totaltargetcycles;
		}
		currentcycles = 0;
		
		totaltargetcycles += targetcycles;
		//totalcurrentcycles += currentcycles;

		//System.out.print('-');
		//System.out.println("tick: " + currenttargetcycles);
		ticks++;
		
		//if ((ticks % 60) == 0) {
		//	vdpInterruptFrac = 0;
		//}
	}

	public synchronized boolean isThrottled() {
		return (currentcycles >= currenttargetcycles);
	}

	public void access(boolean read, boolean word, int cycles) {
		addCycles(cycles);
	}

	public synchronized int getCurrentCycleCount() {
		return currentcycles;
	}

	public synchronized int getCurrentTargetCycleCount() {
		return currenttargetcycles;
	}

	public synchronized long getTotalCycleCount() {
		return totalcurrentcycles;
	}
	
	public synchronized long getTotalCurrentCycleCount() {
		return totalcurrentcycles + currentcycles;
	}
	
	/** Get the tick count, in ms */
	public synchronized int getTickCount() {
		return ticks;
	}

	public boolean isAllowInts() {
		return allowInts;
	}

	public void setAllowInts(boolean allowInts) {
		this.allowInts = allowInts;
	}

	public void setCruAccess(CruAccess access) {
		this.cruAccess = access;
	}

	public CruAccess getCruAccess() {
		return cruAccess;
	}

	public void saveState(IDialogSettings section) {
		section.put("PC", PC);
		section.put("WP", WP);
		section.put("status", status.flatten());
		section.put("ForceAllIntsLevel1", forceIcTo1);
		settingRealTime.saveState(section);
		settingCyclesPerSecond.saveState(section);
		cruAccess.saveState(section.addNewSection("CRU"));
	}

	public void loadState(IDialogSettings section) {
		if (section == null) {
			setPin(INTLEVEL_RESET);
			return;
		}
		
		PC = (short) section.getInt("PC");
		WP = (short) section.getInt("WP");
		status.expand((short) section.getInt("status"));
		forceIcTo1 = section.getBoolean("ForceAllIntsLevel1");
		settingRealTime.loadState(section);
		settingCyclesPerSecond.loadState(section);
		cruAccess.loadState(section.getSection("CRU"));
	}

	/** Get target # cycles to be executed per tick */
	public int getTargetCycleCount() {
		return targetcycles;
	}
	
	public int getAndResetInterruptCount() {
		int n = interrupts;
		interrupts = 0;
		return n;
	}

	public void addAllowedCycles(int i) {
		currenttargetcycles += i;
		
	}

	public void resetCycleCounts() {
		currenttargetcycles = currentcycles = 0;
		totalcurrentcycles = totaltargetcycles = 0;
	}

}