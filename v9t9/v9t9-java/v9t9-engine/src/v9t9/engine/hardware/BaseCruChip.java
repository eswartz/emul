/*
  BaseCruChip.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.hardware;


import java.io.PrintWriter;

import org.apache.log4j.Logger;

import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import v9t9.common.cpu.ICpu;
import v9t9.common.hardware.ICruChip;
import v9t9.common.machine.IMachine;

/**
 * CRU handlers for the F99 machine.
 * @author ejs
 */
public class BaseCruChip implements ICruChip {
	static final Logger logger = Logger.getLogger(BaseCruChip.class);
	
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

	protected boolean intreq;

	private final int intCount;

	private int prevClockRegister;

	private IProperty dumpFullInstructions;
	
    public BaseCruChip(IMachine machine, int intCount) {
        this.machine = machine;
		this.intCount = intCount;
		dumpFullInstructions = machine.getSettings().get(ICpu.settingDumpFullInstructions);
		

        reset();
    }
    protected void log(String msg) {
		PrintWriter pw = Logging.getLog(dumpFullInstructions);
		if (pw != null)
			pw.println("[CRU] " + msg);
	}
    public void reset() {
        enabledIntMask = 0;
        intreq = false;
        ic = 0xf;
		// 	XXX: reset software pins
        currentints = 0;
        clockRegister = 0;
        clockmode = false;
        leftoverCycles = 0;
	}

	protected void resetClock() {
		if (clockRegister != prevClockRegister) {
			log("new clock register: " + clockRegister+"; rate = " + 
					(3000000 / clockRegister / 64) + " Hz");
			logger.info("new clock register: " + clockRegister+"; rate = " + 
					(3000000 / clockRegister / 64) + " Hz");
			prevClockRegister = clockRegister;
		}
		clockDecrementerRegister = clockRegister;
		prevCycles = machine.getCpu().getCurrentCycleCount() + machine.getCpu().getTotalCycleCount();
		leftoverCycles = 0;
		//clockTargetCycleCount = machine.getCpu().getTotalCurrentCycleCount() + 64;
	}

    public boolean isInterruptWaiting() {
    	return intreq;
    }

    public byte getInterruptLevel() {
    	return ic;
    }

  
  
    /** When PIN_INTREQ set, the interrupt level (IC* bits on the TMS9900). */
    private byte ic;

	private long prevCycles;
	private int leftoverCycles;

	public void pollForPins(ICpu cpu) {
		// interrupts not generated in clock mode
		if (clockmode) {
			return;
		}
		
		// while polling, also handle clock if in I/O mode
		final int CYCLES_PER_TICK = 64;
		if (clockRegister != 0) {
			// this decrements once every N cycles
			long nowCycles = cpu.getCurrentCycleCount() + cpu.getTotalCycleCount();
			long diff = nowCycles >= prevCycles ? nowCycles - prevCycles : nowCycles;
			diff += leftoverCycles;
			prevCycles = nowCycles;
			
			while (diff >= CYCLES_PER_TICK) {
				diff -= CYCLES_PER_TICK;
				if (--clockDecrementerRegister < 0) {
					if ((enabledIntMask & (1 << intClock)) != 0) {
						//logger.debug("tick");
						if (!suppressClockInterrupts) {
							triggerInterrupt(intClock);
							
							// "When the clock interrupt is active, the clock mask must be written
							// to clear the interrupt."
							suppressClockInterrupts = true;
						}
						clockReadRegister = clockDecrementerRegister;
					} else {
						clockReadRegister = clockDecrementerRegister;
					}
					resetClock();
					break;
				}
			}
			
			leftoverCycles = (int) diff;
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

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ICruChip#handledInterrupt()
	 */
	@Override
	public void handledInterrupt() {
		intreq = false;		
	}
	/**
		Trigger an interrupt, via hardware.
	*/
	public void triggerInterrupt(int level) {
		if ((enabledIntMask & (1 << level)) != 0) {
			if ((currentints & (1 << level)) == 0) {
				currentints |= 1 << level;
				machine.getCpu().setIdle(false);
				//System.out.println("Hardware triggered interrupt... "+level+"/"+currentints);
			}
		}
	}

	public void acknowledgeInterrupt(int level) {
		if ((currentints & (1 << level)) != 0) {
			currentints &= ~(1 << level);
			machine.getCpu().acknowledgeInterrupt(level);
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
