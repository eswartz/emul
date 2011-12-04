/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9.machine.ti99.machine;

import v9t9.common.keyboard.IKeyboardState;
import v9t9.engine.hardware.BaseCruChip;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.hardware.ICruReader;
import v9t9.engine.hardware.ICruWriter;

/**
 * CRU handlers for the TMS9901 (as attached to a TI-99/4A).
 * @author ejs
 */
public class InternalCru9901 extends BaseCruChip {
	private CruManager manager;

	private ICruWriter cruw9901_0 = new ICruWriter() {

		public int write(int addr, int data, int num) {
			clockmode = (data != 0);
			if (clockmode) {
				clockReadRegister = clockDecrementerRegister;
			}
			return 0;
		}
		
	};
	
	/*
	Change an interrupt enable, or change bit in clock interval
	*/
	private ICruWriter cruw9901_S = new ICruWriter() {

		public int write(int addr, int data, int num) {
			int         bit = addr / 2;
		
			if (clockmode) {
				clockRegister =
					(clockRegister & ~(1 << bit)) | (data << bit);
				resetClock();
				//logger(_L | L_2, "cruw9901_S:  hw9901.latchedclockinvl=%04X\n",
					// hw9901.latchedclockinvl);
			} else {
				//int         mask = (~((~0) << num)) << bit;
				int mask = 1 << bit;
		
				//logger(_L | L_2, _("Altering 9901 bit... addr=%04X, data=%04X,mask=%04X\n"), addr,
				//	 data, mask);
		
				if (bit == intClock)
					suppressClockInterrupts= false;
				
				//  First, writing a 0 will disable the interrupt,
				//  and writing a 1 will enable it, or acknowledge it.
				if (data == 0) {
					enabledIntMask &= ~mask;
				} else { 
					if ((currentints & mask) != 0)
						acknowledgeInterrupt(bit);
					enabledIntMask |= mask;
				}
				
				//logger(_L | L_2, _("before reset: enabledIntMask = %04X, currentints = %04X\n"), hw9901.enabledIntMask,
				//	 hw9901.currentints);
		
				//logger(_L | L_2, _("after reset: enabledIntMask = %04X, currentints = %04X\n"), hw9901.enabledIntMask,
				//	 hw9901.currentints);
		
				/*  enabledIntMaskchange();
				   handle9901(); */
			}
			return 0;
		}
	};
	
	private ICruWriter cruw9901_reset = new ICruWriter() {
		
		public int write(int addr, int data, int num) {
			if (clockmode) {
				reset();
			}
			return 0;
		}
		
	};
	private ICruWriter cruwkeyboard_2 = new ICruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 3) | (data << 2);
			return 0;
		}
		
	};

	private ICruWriter cruwkeyboard_1 = new ICruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 5) | (data << 1);
			return 0;
		}
		
	};

	private ICruWriter cruwkeyboard_0 = new ICruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 6) | (data);
			return 0;
		}
		
	};

	private ICruWriter cruwAlpha = new ICruWriter() {

		public int write(int addr, int data, int num) {
			if (data != 0) {
				keyboardState.resetProbe();
			}
			alphaLockMask = data != 0;
			return 0;
		}
		
	};
	private ICruWriter cruwAudioGate = new ICruWriter() {
		
		public int write(int addr, int data, int num) {
			getMachine().getSound().setAudioGate(addr, data != 0);
			return 0;
		}
		
	};

	private ICruReader crur9901_0 = new ICruReader() {
		public int read(int addr, int data, int num) {
			return clockmode ? 1 : 0;
		}
	};
	
	/*	Read INT_EXT status or lowest bit of timer.  */
	private ICruReader crur9901_1 = new ICruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return clockReadRegister & 1;
			else if ((enabledIntMask & (1 << intExt)) != 0) {
				return (currentints & (1 << intExt)) == 0 ? 0 : 1;
			} else
				return 0;
		}
	};

	private ICruReader crur9901_2 = new ICruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return (clockReadRegister >> 1) & 1;
			else if ((enabledIntMask & (1 << intVdp)) != 0) {
				// if the keyboard is not scanned continuously, this
				// is a way to trap it in the standard TI ROM
				keyboardState.resetProbe();
				return (currentints & (1 << intVdp)) == 0 ? 0 : 1;
			} else
				return 0;
		}
	};
	
	private ICruReader crur9901_KS = new ICruReader() {

		public int read(int addr, int data, int num) {
			int mask;
			int bit = addr / 2;

			mask = 1 << (bit - 3);

			if (clockmode)
				return (clockReadRegister >> (bit - 1)) & 0x1;
			else if ((enabledIntMask & (1 << bit)) != 0)
				return (currentints & (1 << bit)) == 0 ? 0 : 1;
			else {
				int alphamask = 0;
				
				if (!alphaLockMask && mask == 0x10) {
					alphamask = keyboardState.getAlpha() ? 0 : 0x10;
				}
				int colMask = (keyboardState.getKeyboardRow(crukeyboardcol) & mask);
				int colBits = (colMask | alphamask);
				return colBits != 0 ? 0 : 1;
			}
		}
		
	};
	private ICruReader crur9901_15 = new ICruReader() {
	
		public int read(int addr, int data, int num) {
			if (clockmode) {
				return intreq ? 1 : 0;
			}
			return 0;
		}
		
	};
	private ICruReader cruralpha = new ICruReader() {

		public int read(int addr, int data, int num) {
			keyboardState.setProbe();
			
			return keyboardState.getAlpha() ? 1 : 0;
		}
		
	};
	
    public InternalCru9901(TI99Machine machine, IKeyboardState keyboardState) {
    	super(machine, keyboardState, 15);
    	
    	intExt = 1;
    	intVdp = 2;
    	intClock = 3;
    	
        this.manager = machine.getCruManager();
        assert manager != null;

        registerInternalCru(0x0, 1, cruw9901_0);
        registerInternalCru(0x2, 1, cruw9901_S);
        registerInternalCru(0x4, 1, cruw9901_S);
        registerInternalCru(0x6, 1, cruw9901_S);
        registerInternalCru(0x8, 1, cruw9901_S);
        registerInternalCru(0xa, 1, cruw9901_S);
        registerInternalCru(0xc, 1, cruw9901_S);
        registerInternalCru(0xe, 1, cruw9901_S);
        registerInternalCru(0x10, 1, cruw9901_S);
        registerInternalCru(0x12, 1, cruw9901_S);
        registerInternalCru(0x14, 1, cruw9901_S);
        registerInternalCru(0x16, 1, cruw9901_S);
        registerInternalCru(0x18, 1, cruw9901_S);
        registerInternalCru(0x1a, 1, cruw9901_S);
        registerInternalCru(0x1c, 1, cruw9901_S);
        registerInternalCru(0x1e, 1, cruw9901_reset);
        
        registerInternalCru(0x24, 1, cruwkeyboard_0);
        registerInternalCru(0x26, 1, cruwkeyboard_1);
        registerInternalCru(0x28, 1, cruwkeyboard_2);
        registerInternalCru(0x2A, 1, cruwAlpha);

        registerInternalCru(0x30, 1, cruwAudioGate);
        
        registerInternalCru(0x0, 1, crur9901_0);
        registerInternalCru(0x2, 1, crur9901_1);
        registerInternalCru(0x4, 1, crur9901_2);
        registerInternalCru(0x6, 1, crur9901_KS);
        registerInternalCru(0x8, 1, crur9901_KS);
        registerInternalCru(0xa, 1, crur9901_KS);
        registerInternalCru(0xc, 1, crur9901_KS);
        registerInternalCru(0xe, 1, crur9901_KS);
        registerInternalCru(0x10, 1, crur9901_KS);
        registerInternalCru(0x12, 1, crur9901_KS);
        registerInternalCru(0x14, 1, crur9901_KS);
        registerInternalCru(0x1e, 1, crur9901_15);
        registerInternalCru(0x2a, 1, cruralpha);

    }

	protected void resetClock() {
		clockDecrementerRegister = clockRegister;
		clockTargetCycleCount = getMachine().getCpu().getTotalCurrentCycleCount() + 64;
		//System.out.println("Reset clock to " + clockRegister);
	}

	/** Register handler for a range of bits.  Note that the internal bus
     * aliases in blocks of 0x40.
     * @param addr
     * @param bits
     * @param writer
     */
	private void registerInternalCru(int addr, int bits, ICruWriter writer) {
		while (addr < 0x400) {
			manager.add(addr, bits, writer);
			addr += 0x40;
		}
	}

    /** Register handler for a range of bits.  Note that the internal bus
     * aliases in blocks of 0x40.
     * @param addr
     * @param bits
     * @param writer
     */
	private void registerInternalCru(int addr, int bits, ICruReader reader) {
		while (addr < 0x400) {
			manager.add(addr, bits, reader);
			addr += 0x40;
		}
	}

    /** Access the registration object for CRU handlers */
    public CruManager getCruManager() {
    	return manager;
    }
}
