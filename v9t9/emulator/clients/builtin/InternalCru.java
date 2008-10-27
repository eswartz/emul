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
import v9t9.engine.CruHandler;
import v9t9.keyboard.KeyboardState;
import v9t9.utils.Utils;

/**
 * CRU implementation for the internal 99/4A hardware
 * @author ejs
 */
public class InternalCru implements CruHandler {
    Machine machine;
	private CruManager manager;

	private KeyboardState keyboardState;
	private int crukeyboardcol;
	private boolean AlphaLock;
	
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
			AlphaLock = data != 0;
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

	private CruReader crur9901_2 = new CruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode)
				return (latchedtimer >> 1) & 1;
			else if ((int9901 & M_INT_VDP) != 0) {
				//logger(_L | L_1, "crur9901_2: currentints=%04X\n", hw9901.currentints);
				//return !(hw9901.currentints&M_INT_EXT) && !!(hw9901.currentints & M_INT_VDP);
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
					((bit - 3) == 4) ? ((AlphaLock || !caps) ? 0 : 0x10) : 0;

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
	
    public InternalCru(Machine machine, KeyboardState keyboardState) {
        this.machine = machine;
		this.keyboardState = keyboardState;
        this.manager = new CruManager();
        
        manager.add(0x24, 1, cruwkeyboard_2);
        manager.add(0x26, 1, cruwkeyboard_1);
        manager.add(0x28, 1, cruwkeyboard_0);
        manager.add(0x2A, 1, cruwAlpha);
		
        manager.add(0x4, 1, crur9901_2);
        manager.add(0x6, 1, crur9901_KS);
        manager.add(0x8, 1, crur9901_KS);
        manager.add(0xa, 1, crur9901_KS);
        manager.add(0xc, 1, crur9901_KS);
        manager.add(0xe, 1, crur9901_KS);
        manager.add(0x10, 1, crur9901_KS);
        manager.add(0x12, 1, crur9901_KS);
        manager.add(0x14, 1, crur9901_KS);
        manager.add(0x2a, 1, cruralpha);
    }

    /** Access the registration object for CRU handlers */
    public CruManager getCruManager() {
    	return manager;
    }
    
    public void writeBits(int addr, int val, int num) {
        addr &= 0x1fff;

        System.out.println("CRU write: >" + Utils.toHex4(addr) + "[" + num + "], " + Utils.toHex4(val));

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
        System.out.print("CRU read: >" + Utils.toHex4(addr) + "[" + num + "] = ");
        
    	if (addr >= 0x30) {
    	    // TODO
    		//setclockmode9901(0);
    	}
        
    	int val = manager.readBits(addr, num);
        System.out.println(Utils.toHex4(val));

        return val;
    }
    
    
}
