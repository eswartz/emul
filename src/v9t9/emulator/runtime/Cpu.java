/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime;

import v9t9.emulator.Machine;
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
	/** current handled interrupt level */
	private byte intlevel;
	long lastInterrupt;
	/* interrupt pins */
	public static final int INTPIN_RESET = 1;
	public static final int INTPIN_LOAD = 2;
	public static final int INTPIN_INTREQ = 4;
	
	/*	Variables for controlling a "real time" emulation of the 9900
	processor.  Each call to execute() sets an estimated cycle count
	for the instruction and parameters in "instcycles".  We waste
	time in 1/BASE_EMULATOR_HZ second quanta to maintain the appearance of a
	3.0 MHz clock. */
	
	int         baseclockhz;
	
	int         instcycles;	// cycles for each instruction
	
	int         targetcycles;	// target # cycles to be executed per tick
	int         currenttargetcycles;	// target # cycles to be executed for this tick
	long         totaltargetcycles;	// total # target cycles expected
	int         currentcycles = 0;	// current cycles per tick
	long         totalcurrentcycles;	// total # current cycles executed
	private int interruptTick;	// # ms between CPU syncs
	

    public Cpu(Machine machine, int interruptTick) {
        this.machine = machine;
        this.memory = machine.getMemory();
        this.console = machine.getConsole();
        this.console.setAccessListener(this);
        this.status = new Status();
        this.baseclockhz = settingCyclesPerSecond.getInt();
        this.interruptTick = interruptTick;
        this.targetcycles = (int)((long) baseclockhz * interruptTick / 1000);
        this.currenttargetcycles = this.targetcycles;
        System.out.println("target: " + targetcycles);
        
        settingCyclesPerSecond.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				baseclockhz = setting.getInt();
				targetcycles = baseclockhz / Cpu.this.interruptTick;
				currenttargetcycles = targetcycles;
			}
        	
        });
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

    public void holdpin(int mask) {
        intpins |= (byte) mask;
        //abortIfInterrupted();
    }

    private byte intpins;
	private int ticks;
	private boolean allowInts;

	static public final String sRealTime = "RealTime";

	static public final Setting settingRealTime = new Setting(
			sRealTime, new Boolean(false));

	static public final String sCyclesPerSecond = "CyclesPerSecond";

	static public final Setting settingCyclesPerSecond = new Setting(
			sCyclesPerSecond, new Integer(3300000));

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
            holdpin(INTPIN_INTREQ);
        }
    }

    /**
     * Called by compiled code to see if it's time to stop
     * running.  All this can do is throw AbortedException().
     */
    public void abortIfInterrupted() {
    	if (intpins != 0) {
    		if (status.getIntMask() != 0)
    			intlevel = (byte) status.getIntMask();
    		else if ((intpins & INTPIN_LOAD) != 0)
    			intlevel = 1;
        }
        if (intlevel != 0) {
           throw new AbortedException();
        }
            
    }
    
    public void handleInterrupts() {
        if (intlevel != 0) {
            handleInterrupt();
        }
    }

    /**
     *  
     */
    private void handleInterrupt() {
        //      any sort of interrupt that sets intpins9900

        // non-maskable
        if ((intpins & INTPIN_LOAD) != 0) {
        	intlevel = 0;	 // ???
            intpins &= ~INTPIN_LOAD;
            //logger(_L | 0, "**** NMI ****");
            System.out.println("**** NMI ****");
            contextSwitch(0xfffc);
            //instcycles += 22;
            //execute_current_inst();
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else
        // non-maskable (?)
        if ((intpins & INTPIN_RESET) != 0) {
            intpins &= ~INTPIN_RESET;
            //logger(_L | 0, "**** RESET ****\n");
            System.out.println("**** RESET ****");
            contextSwitch(0);
            //instcycles += 26;
            //execute_current_inst();
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else
        // maskable
        if ((intpins & INTPIN_INTREQ) != 0) {
            //short highmask = (short) (1 << (intlevel + 1));

            // 99/4A console hardcodes all interrupts as level 1,
            // so if any are available, level 1 is it.
            if (intlevel != 0
            // TODO: && read9901int() &&
            //(!(stateflag & ST_DEBUG) || (allow_debug_interrupts))
            ) {
                //System.out.println("**** INT ****");
                intpins &= ~INTPIN_INTREQ;
                contextSwitch(0x4);
                intlevel = 0;
                
                //instcycles += 22;
                //execute_current_inst();
                
                // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
                machine.getExecutor().interpretOneInstruction();
                //machine.getExecutor().execute();
                //throw new AbortedException();
            }
        } else {
			intpins = 0; // invalid
		}

        //if (intpins == 0)
        //	stateflag &= ~ST_INTERRUPT;
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
		//if (currentcycles > targetcycles)
		//	System.out.print('!');
	}

	public synchronized void tick() {
		totaltargetcycles += targetcycles;
		totalcurrentcycles += currentcycles;
		
		// if we went over, aim for fewer this time
		currenttargetcycles = (int) (totaltargetcycles - totalcurrentcycles);
		currentcycles = 0;
		//System.out.print('-');
		//System.out.println("tick: " + currenttargetcycles);
		ticks++;
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

	public synchronized long getTotalCycleCount() {
		return totalcurrentcycles;
	}
	
	public synchronized int getTickCount() {
		return ticks;
	}

	public boolean isAllowInts() {
		return allowInts;
	}

	public void setAllowInts(boolean allowInts) {
		this.allowInts = allowInts;
	} 

}