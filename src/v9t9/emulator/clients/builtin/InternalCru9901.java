/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.emulator.hardware.CruManager;
import v9t9.emulator.hardware.CruReader;
import v9t9.emulator.hardware.CruWriter;
import v9t9.emulator.runtime.Cpu;
import v9t9.keyboard.KeyboardState;

/**
 * CRU handlers for the TMS9901 (as attached to a TI-99/4A)
 * @author ejs
 */
public class InternalCru9901 implements CruAccess {
    Machine machine;
	private CruManager manager;

	private int crukeyboardcol;
	/** Set to prevent reading alpha lock */
	private boolean alphaLockMask;
	protected int latchedclockinvl;
	
	/*
	Change an interrupt enable, or change bit in clock interval
	*/
	private CruWriter cruw9901_S = new CruWriter() {

		public int write(int addr, int data, int num) {
			int         bit = addr / 2;
		
			if (clockmode) {
				latchedclockinvl =
					(latchedclockinvl & ~(1 << bit)) | (data << bit);
				//logger(_L | L_2, "cruw9901_S:  hw9901.latchedclockinvl=%04X\n",
					// hw9901.latchedclockinvl);
			} else {
				//int         mask = (~((~0) << num)) << bit;
				int mask = 1 << bit;
		
				//logger(_L | L_2, _("Altering 9901 bit... addr=%04X, data=%04X,mask=%04X\n"), addr,
				//	 data, mask);
		
				//  First, writing a 0 will disable the interrupt,
				//  and writing a 1 will enable it, or acknowledge it.
				if (data == 0) {
					int9901 &= ~mask;
				} else { 
					if ((currentints & mask) != 0)
						acknowledgeInterrupt(bit);
					int9901 |= mask;
				}
				
				//logger(_L | L_2, _("before reset: int9901 = %04X, currentints = %04X\n"), hw9901.int9901,
				//	 hw9901.currentints);
		
				//logger(_L | L_2, _("after reset: int9901 = %04X, currentints = %04X\n"), hw9901.int9901,
				//	 hw9901.currentints);
		
				/*  int9901change();
				   handle9901(); */
			}
			return 0;
		}
	};
	
	private CruWriter cruwkeyboard_2 = new CruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 3) | (data << 2);
			return 0;
		}
		
	};

	private CruWriter cruwkeyboard_1 = new CruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 5) | (data << 1);
			return 0;
		}
		
	};

	private CruWriter cruwkeyboard_0 = new CruWriter() {

		public int write(int addr, int data, int num) {
			crukeyboardcol = (crukeyboardcol & 6) | (data);
			return 0;
		}
		
	};

	private CruWriter cruwAlpha = new CruWriter() {

		public int write(int addr, int data, int num) {
			alphaLockMask = data != 0;
			return 0;
		}
		
	};
	protected boolean clockmode;
	protected int latchedtimer;
	
	/** Enabled interrupt mask.  Any request to trigger an interrupt not
	 * enabled in this mask will be ignored.
	 */
	protected int int9901;
	/** Currently active interrupts.  These are fed to the TMS9900
	 * via {@link Cpu#setInterruptRequest(byte)} in priority order. */
	protected int currentints;

	/** intlevel for peripheral interrupt */ 
	public static final int INT_EXT = 1;
	/** int9901/currentints mask for peripheral interrupt */ 
	public static final int M_INT_EXT = 2;
	/** intlevel for VDP interrupt */ 
	public static final int INT_VDP = 2;
	/** int9901/currentints mask for VDP interrupt */ 
	public static final int M_INT_VDP = 4;
	/** intlevel for clock interrupt */ 
	public static final int INT_CLOCK = 3;
	/** int9901/currentints mask for clock interrupt */ 
	public static final int M_INT_CLOCK = 8;

	private CruReader crur9901_0 = new CruReader() {
		public int read(int addr, int data, int num) {
			return 1;
		}
	};
	
	/*	Read INT_EXT status or lowest bit of timer.  */
	private CruReader crur9901_1 = new CruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return latchedtimer & 1;
			else if ((int9901 & M_INT_EXT) != 0) {
				return (currentints & M_INT_EXT) == 0 ? 0 : 1;
			} else
				return 0;
		}
	};

	private CruReader crur9901_2 = new CruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return (latchedtimer >> 1) & 1;
			else if ((int9901 & M_INT_VDP) != 0) {
				return (currentints & M_INT_VDP) == 0 ? 0 : 1;
			} else
				return 0;
		}
	};
	
	private CruReader crur9901_KS = new CruReader() {

		public int read(int addr, int data, int num) {
			int mask;
			int bit = addr / 2;

			mask = 1 << (bit - 3);

			if (clockmode)
				return (latchedtimer >> (bit - 1)) & 0x1;
			else if ((int9901 & (1 << bit)) != 0)
				return (currentints & ~(~0 << bit)) == 0 
					&& (currentints & (1 << bit)) != 0 ? 1 : 0;
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
	private CruReader cruralpha = new CruReader() {

		public int read(int addr, int data, int num) {
			keyboardState.resetProbe();
			return keyboardState.getAlpha() ? 1 : 0;
		}
		
	};
	private int intlevel;
	private final KeyboardState keyboardState;
	
    public InternalCru9901(Machine machine, KeyboardState keyboardState) {
        this.machine = machine;
		this.keyboardState = keyboardState;
        this.manager = machine.getCruManager();
        

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
        
        registerInternalCru(0x24, 1, cruwkeyboard_0);
        registerInternalCru(0x26, 1, cruwkeyboard_1);
        registerInternalCru(0x28, 1, cruwkeyboard_2);
        registerInternalCru(0x2A, 1, cruwAlpha);
		
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
        registerInternalCru(0x2a, 1, cruralpha);
    }

    /** Register handler for a range of bits.  Note that the internal bus
     * aliases in blocks of 0x40.
     * @param addr
     * @param bits
     * @param writer
     */
	private void registerInternalCru(int addr, int bits, CruWriter writer) {
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
	private void registerInternalCru(int addr, int bits, CruReader reader) {
		while (addr < 0x400) {
			manager.add(addr, bits, reader);
			addr += 0x40;
		}
	}

    /** Access the registration object for CRU handlers */
    public CruManager getCruManager() {
    	return manager;
    }
    
    
	public void pollForPins(Cpu cpu) {
		// any of these interrupts enabled?  [optimization]
		cpu.resetInterruptRequest();
		if ((currentints & int9901) != 0) {
			intlevel = 15;
			while (intlevel != 0 && ((currentints & int9901) & (1 << intlevel)) == 0)
				intlevel--;
	
			if (intlevel != 0) {
				//System.out.println(
				//		"Requesting interrupt... "+intlevel+"/"+currentints+"/"+int9901);
				
				cpu.setInterruptRequest((byte) intlevel);
			}
		}
	}

	/**
		Trigger an interrupt, via hardware.
	*/
	public void triggerInterrupt(int level) {
		if ((int9901 & (1 << level)) != 0) {
			if ((currentints & (1 << level)) == 0) {
				currentints |= 1 << level;
				//System.out.println(
				//		"Hardware triggered interrupt... "+level+"/"+currentints+"/"+int9901);
			}
		}
	}

	public void acknowledgeInterrupt(int level) {
		if ((currentints & (1 << level)) != 0) {
			currentints &= ~(1 << level);
			//System.out.println(
			//		"Acknowledged interrupt... "+level+"/"+currentints+"/"+int9901);
		} else {
			//System.out.println(
			//		"??? acknowledged unset interrupt... "+level+"/"+currentints+"/"+int9901);
		}
	}
	
}
