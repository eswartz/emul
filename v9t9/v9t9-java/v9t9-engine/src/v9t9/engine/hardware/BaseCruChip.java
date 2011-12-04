/*
 * (c) Ed Swartz, 2010
 *
 */
package v9t9.engine.hardware;


import v9t9.base.settings.ISettingSection;
import v9t9.common.cpu.ICpu;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.engine.memory.IMachine;

/**
 * CRU handlers for the F99 machine.
 * @author ejs
 */
public class BaseCruChip implements ICruChip {
    IMachine machine;

	/** intlevel for peripheral interrupt */ 
	public int intExt = 1;
	/** intlevel for VDP interrupt */ 
	public int intVdp = 2;
	/** intlevel for clock interrupt */ 
	public int intClock = 3;

	
    protected int crukeyboardcol;
	/** Set to prevent reading alpha lock */
	protected boolean alphaLockMask;
	protected int clockRegister;
	protected int clockDecrementerRegister;
	protected int clockReadRegister;
	protected boolean suppressClockInterrupts;
	
	protected boolean clockmode;
	
	/** Enabled interrupt mask.  Any request to trigger an interrupt not
	 * enabled in this mask will be ignored.
	 */
	protected int enabledIntMask;
	/** Currently active interrupts.  These are fed to the TMS9900
	 * via {@link Cpu9900#setInterruptRequest(byte)} in priority order. */
	protected int currentints;

	protected final IKeyboardState keyboardState;
	protected long clockTargetCycleCount;
	protected boolean intreq;

	private final int intCount;
	
    public BaseCruChip(IMachine machine, IKeyboardState keyboardState, int intCount) {
        this.machine = machine;
		this.keyboardState = keyboardState;
		this.intCount = intCount;
        
        reset();
    }

    public void reset() {
        enabledIntMask = 0;
        intreq = false;
        ic = 0xf;
		// 	XXX: reset software pins
        currentints = 0;
	}

	protected void resetClock() {
		clockDecrementerRegister = clockRegister;
		clockTargetCycleCount = machine.getCpu().getTotalCurrentCycleCount() + 64;
		//System.out.println("Reset clock to " + clockRegister);
	}

    public boolean isInterruptWaiting() {
    	return intreq;
    }

    public byte getInterruptLevel() {
    	return ic;
    }

  
  
    /** When PIN_INTREQ set, the interrupt level (IC* bits on the TMS9900). */
    private byte ic;
    
	public void pollForPins(ICpu cpu) {
		// interrupts not generated in clock mode
		if (clockmode) {
			return;
		}
		
		// while polling, also handle clock if in I/O mode
		if (clockRegister != 0) {
			// this decrements once every 64 cycles
			long nowCycles = cpu.getTotalCurrentCycleCount();
			while (clockTargetCycleCount < nowCycles) {
				if (--clockDecrementerRegister <= 0) {
					//System.out.println("tick");
					if (!suppressClockInterrupts && (enabledIntMask & (1 << intClock)) != 0) {
						triggerInterrupt(intClock);
						
						// "When the clock interrupt is active, the clock mask must be written
						// to clear the interrupt."
						suppressClockInterrupts = true;
					}
					resetClock();
					break;
				}
				clockTargetCycleCount += 64;
				clockReadRegister = clockDecrementerRegister;
			}
		}
		
		intreq = false;
		
		if ((currentints & enabledIntMask) != 0) {
			int intlevel;
			
			intlevel = intCount - 1;
			while (intlevel != 0 && ((currentints & enabledIntMask) & (1 << intlevel)) == 0)
				intlevel--;
	
			if (intlevel != 0) {
				//System.out.println("Requesting interrupt... "+intlevel+"/"+currentints+"/"+currentints);

				this.intreq = true;
				this.ic = ((byte) intlevel);
				cpu.irq();
			}
		}
	}

	/**
		Trigger an interrupt, via hardware.
	*/
	public void triggerInterrupt(int level) {
		if ((enabledIntMask & (1 << level)) != 0) {
			if ((currentints & (1 << level)) == 0) {
				currentints |= 1 << level;
				machine.getCpu().setIdle(false);
				//System.out.println(
				//		"Hardware triggered interrupt... "+level+"/"+currentints+"/"+int9901);
			}
		}
	}

	public void acknowledgeInterrupt(int level) {
		if ((currentints & (1 << level)) != 0) {
			currentints &= ~(1 << level);
			machine.getCpu().acknowledgeInterrupt();
			//System.out.println(
			//		"Acknowledged interrupt... "+level+"/"+currentints+"/"+int9901);
		} else {
			//System.out.println(
			//		"??? acknowledged unset interrupt... "+level+"/"+currentints+"/"+int9901);
		}
	}

	public void saveState(ISettingSection section) {
		section.put("EnabledInterrupts", enabledIntMask);
		section.put("CurrentInterrupts", currentints);
		section.put("KeyboardColumn", crukeyboardcol);
		section.put("ClockMode", clockmode);
		section.put("ClockRegister", clockRegister);
		section.put("ClockReadRegister", clockReadRegister);
		section.put("ClockDecrementerRegister", clockDecrementerRegister);
		section.put("SuppressClockInterrupts", suppressClockInterrupts);
		section.put("AlphaLockMask", alphaLockMask);
		section.put("IntReq", intreq);
		section.put("IC", ic);
	}
	public void loadState(ISettingSection section) {
		if (section == null) {
			reset();
			return;
		}
		enabledIntMask = section.getInt("EnabledInterrupts");
		currentints = section.getInt("CurrentInterrupts");
		crukeyboardcol = section.getInt("KeyboardColumn");
		clockmode = section.getBoolean("ClockMode");
		clockReadRegister = section.getInt("ClockReadRegister");
		clockDecrementerRegister = section.getInt("ClockDecrementerRegister");
		clockRegister = section.getInt("ClockRegister");
		suppressClockInterrupts = section.getBoolean("SuppressClockInterrupts");
		alphaLockMask = section.getBoolean("AlphaLockMask");
		intreq = section.getBoolean("IntReq");
		ic = (byte) section.getInt("IC");
	}

	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}

}
