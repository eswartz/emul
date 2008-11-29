/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruManager;
import v9t9.emulator.hardware.CruReader;
import v9t9.emulator.hardware.CruWriter;
import v9t9.emulator.runtime.Cpu;
import v9t9.engine.CruHandler;
import v9t9.keyboard.KeyboardState;

/**
 * CRU implementation for the internal 99/4A hardware
 * @author ejs
 */
public class InternalCru implements CruHandler {
    Machine machine;
	private CruManager manager;

	private KeyboardState keyboardState;
	private int crukeyboardcol;
	private boolean alphaLock;
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
				int         mask = (~((~0) << num)) << bit;
		
				//logger(_L | L_2, _("Altering 9901 bit... addr=%04X, data=%04X,mask=%04X\n"), addr,
				//	 data, mask);
		
				//  First, writing a 0 will disable the interrupt,
				//  and writing a 1 will enable it, or acknowledge it.
				int9901 = (int9901 & ~mask) | (data << bit);
				//logger(_L | L_2, _("before reset: int9901 = %04X, currentints = %04X\n"), hw9901.int9901,
				//	 hw9901.currentints);
		
				//  This will acknowledge the interrupt, and possibly
				//  trigger a lower-level pending interrupt.
				reset9901int(data << bit);
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
			alphaLock = data != 0;
			return 0;
		}
		
	};
	protected boolean clockmode;
	protected int latchedtimer;
	protected int int9901;
	protected int currentints;
	protected boolean caps;

	public static final int M_INT_EXT = 2;	// peripheral
	public static final int M_INT_VDP = 4;	// VDP
	public static final int M_INT_CLOCK = 8;	// clock

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
				return (currentints & M_INT_EXT) != 0 ? 0 : 1;
			} else
				return 0;
		}
	};

	private CruReader crur9901_2 = new CruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return (latchedtimer >> 1) & 1;
			else if ((int9901 & M_INT_VDP) != 0) {
				return (currentints & M_INT_VDP) != 0 ? 0 : 1;
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
				int alphamask =
					((bit - 3) == 4) ? ((alphaLock || !caps) ? 0 : 0x10) : 0;

				//logger(_L | L_2, "crukeyboardcol=%X, mask=%X, addr=%2X\n", crukeyboardcol,
				//	 mask, addr);
				return (((keyboardState.getCrukeyboardmap()[crukeyboardcol] & mask) | alphamask)) != 0 ? 0 : 1;
			}
		}
		
	};
	private CruReader cruralpha = new CruReader() {

		public int read(int addr, int data, int num) {
			return 0;
		}
		
	};
	private int intlevel;
	private int intpins;
	
    public InternalCru(Machine machine, KeyboardState keyboardState) {
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
    
    public void writeBits(int addr, int val, int num) {
        addr &= 0x1fff;

        //System.out.println("CRU write: >" + Utils.toHex4(addr) + "[" + num + "], " + Utils.toHex4(val));

    	if (addr < 0x1000) {
    		addr &= 0x3f;
    	}

    	if (addr >= 0x30) {
    	    // TODO
    	//	setclockmode9901(0);
    	}

    	manager.writeBits(addr, val, num);
    }

    /**
     * @param s
     * @param value
     * @return
     */
    public int readBits(int addr, int num) {
        addr &= 0x1fff;
        //logger(_L | L_2, _("CRU read: >%04X[%d] = \n"), addr, num);
       // System.out.print("CRU read: >" + Utils.toHex4(addr) + "[" + num + "] = ");
        
    	if (addr >= 0x30) {
    	    // TODO
    		//setclockmode9901(0);
    	}
        
    	int val = manager.readBits(addr, num);
        //System.out.println(Utils.toHex4(val));

        return val;
    }
    
    /**
	handle9901() is called when something about the 
	interrupt state changes (processor interrupt mask,
	pending interrupt mask, etc).  If an interrupt is needed,
	this triggers the INTPIN_INTREQ pin on the 9900, and sets the 
	ST_INTERRUPT flag in stateflag.
	*/
	void handle9901()
	{
		// any of these interrupts enabled?  [optimization]
		if ((currentints & int9901) != 0) {
			// There are 16 levels, and intlevel is 16 bits.
			// When it goes from 0x8000 to 0, we will see there
			// are no interrupts that can be passed.
			intlevel = M_INT_EXT;
			while (intlevel != 0 && ((currentints & int9901) & intlevel) == 0)
				intlevel =
					(intlevel << 1) & (M_INT_EXT | M_INT_VDP | M_INT_CLOCK);
	
			if (intlevel != 0) {
				System.out.println(
						"Triggering interrupt... "+intlevel+"/"+currentints+"/"+int9901);
	
				intpins |= Cpu.INTPIN_INTREQ;
				//stateflag |= ST_INTERRUPT;
			}
		}
	}

	/**
		Trigger an interrupt, via hardware.
	*/
	void trigger9901int(int mask)
	{
		if ((int9901 & mask) != 0) {
			currentints |= mask;
	
			// see if it applies
			handle9901();
		}
	}
	
	
	/**
		Reset an interrupt, via hardware.
	*/
	void reset9901int(int mask)
	{
		if ((currentints & mask) != 0) {
			currentints &= ~mask;
	
			// take care of pending interrupts
			handle9901();
		}
	}

}
