/*
  BaseCruChip.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.hardware;


import ejs.base.settings.ISettingSection;
import v9t9.common.cpu.ICpu;
import v9t9.common.hardware.ICruChip;
import v9t9.common.machine.IMachine;

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

	protected boolean intreq;

	private final int intCount;
	
    public BaseCruChip(IMachine machine, int intCount) {
        this.machine = machine;
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
		clockDecrementerRegister = (clockRegister >> 1);
		prevCycles = machine.getCpu().getCurrentCycleCount();
		//clockTargetCycleCount = machine.getCpu().getTotalCurrentCycleCount() + 64;
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

	private int prevCycles;

	public void pollForPins(ICpu cpu) {
		// interrupts not generated in clock mode
		if (clockmode) {
			return;
		}
		
		// while polling, also handle clock if in I/O mode
		final int CYCLES_PER_TICK = 64;
		if (clockRegister != 0) {
			// this decrements once every N cycles
			int nowCycles = cpu.getCurrentCycleCount();
			int diff = nowCycles >= prevCycles ? nowCycles - prevCycles : nowCycles;
			prevCycles = nowCycles;
			
			while (diff >= CYCLES_PER_TICK) {
				if (--clockDecrementerRegister < 0) {
					//System.out.println("tick");
					if ((enabledIntMask & (1 << intClock)) != 0) {
						if (!suppressClockInterrupts) {
							triggerInterrupt(intClock);
							
							// "When the clock interrupt is active, the clock mask must be written
							// to clear the interrupt."
							suppressClockInterrupts = true;
							resetClock();
						}
					} else {
						resetClock();
					}
					break;
				}
				diff -= CYCLES_PER_TICK;
				clockReadRegister = clockDecrementerRegister;
			}
			
			prevCycles -= diff;
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
