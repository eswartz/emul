/*
  InternalCru9901.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import static v9t9.common.keyboard.KeyboardConstants.MASK_CAPS_LOCK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.sound.ICassetteVoice;
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
				clockRegister =	(clockRegister & ~(1 << (bit - 1))) | (data << (bit - 1));
				resetClock();
			} else {
				int mask = 1 << bit;
		
				if (bit == intClock)
					suppressClockInterrupts = false;
				
				//  First, writing a 0 will disable the interrupt,
				//  and writing a 1 will enable it, or acknowledge it.
				if (data == 0) {
					enabledIntMask &= ~mask;
				} else { 
					if ((currentints & mask) != 0)
						acknowledgeInterrupt(bit);
					enabledIntMask |= mask;
				}
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
			checkKeyscanPattern(3);
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
				// first CRU bit set in TI ROM keyboard scanning routine, good place to paste
				checkKeyscanPattern(1);
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

	private ICruWriter cruwCassette1 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			getMachine().getCassette().getCassetteVoice().setMotor1(data != 0);
			//System.out.println(System.currentTimeMillis() + ": [CS1] " + (data != 0 ? "on" : "off"));
			return 0;
		}
		
	};
	
	private ICruWriter cruwCassette2 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			getMachine().getCassette().getCassetteVoice().setMotor2(data != 0);
			//System.out.println(System.currentTimeMillis() + ": [CS2] " + (data != 0 ? "on" : "off"));
			return 0;
		}
		
	};
	
	private ICruWriter cruwCassetteOut = new ICruWriter() {
		public int write(int addr, int data, int num) {
			getMachine().getCassette().getCassetteVoice().setState(data != 0);
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
			if (clockmode) {
				return clockReadRegister & 1;
			} else if ((enabledIntMask & (1 << intExt)) != 0) {
				return (currentints & (1 << intExt)) == 0 ? 0 : 1;
			} else {
				return 0;
			}
		}
	};

	private ICruReader crur9901_2 = new ICruReader() {
		public int read(int addr, int data, int num) {
			if (clockmode) {
				return (clockReadRegister >> 1) & 1;
			} else if ((enabledIntMask & (1 << intVdp)) != 0) {
				// if the keyboard is not scanned continuously, this
				// is a way to trap it in the standard TI ROM
				// getMachine().getKeyboardHandler().resetProbe();
				//checkKeyscanPattern(2);
				//System.out.println("Checking VDP interrupt... "+currentints);
				return (currentints & (1 << intVdp)) == 0 ? 0 : 1;
			} else {
				return 0;
			}
		}
	};
	
	private ICruReader crur9901_KS = new ICruReader() {

		public int read(int addr, int data, int num) {
			int bit = addr / 2;

			if (clockmode) {
				return (clockReadRegister >> (bit - 1)) & 0x1;	// addr 6, bit 3, start at 2
			} else if ((enabledIntMask & (1 << bit)) != 0) {
				return (currentints & (1 << bit)) == 0 ? 0 : 1;
			} else {
				if (bit >= 3 && bit < 11) {
					if (bit == 10)
						checkKeyscanPattern(4);
					
					int alphamask = 0;
					
					int mask = 1 << (bit - 3);
					if (!alphaLockMask && mask == 0x10) {
						boolean isCaps = (getMachine().getKeyboardState().getLockMask() & KeyboardConstants.MASK_CAPS_LOCK) != 0;
						alphamask = isCaps ? 0 : 0x10;
					}
					int keyboardRow = getMachine().getKeyboardState().getKeyboardRow(crukeyboardcol);
					int colMask = (keyboardRow & mask);
					int colBits = (colMask | alphamask);
					
					return colBits != 0 ? 0 : 1;
				} else {
					return 0;
				}
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
			getMachine().getKeyboardHandler().setProbe();
			
			boolean isCaps = (getMachine().getKeyboardState().getLockMask() & MASK_CAPS_LOCK) != 0;
			return isCaps ? 1 : 0;
		}
		
	};

	private ICruReader crurCassetteIn = new ICruReader() {
		public int read(int addr, int data, int num) {
			ICassetteVoice voice = getMachine().getCassette().getCassetteVoice();
			int bit = voice.getState() ? 1 : 0;
			return bit;
		}
		
	};

	private List<Integer> knownKeyscanPattern = new LinkedList<Integer>();
	private List<Integer> keyscanPattern = new LinkedList<Integer>();

	private long nextKeyscanPatternCheckTime;

    public InternalCru9901(TI99Machine machine) {
    	super(machine, 15);
    	
    	intExt = 1;
    	intVdp = 2;
    	intClock = 3;
    	
        manager = machine.getCruManager();
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

        registerInternalCru(0x2C, 1, cruwCassette1);
        registerInternalCru(0x2E, 1, cruwCassette2);
        registerInternalCru(0x30, 1, cruwAudioGate);
        registerInternalCru(0x32, 1, cruwCassetteOut);
        
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
        registerInternalCru(0x16, 1, crur9901_KS);
        registerInternalCru(0x18, 1, crur9901_KS);
        registerInternalCru(0x1A, 1, crur9901_KS);
        registerInternalCru(0x1C, 1, crur9901_KS);
        registerInternalCru(0x1e, 1, crur9901_15);
        registerInternalCru(0x2a, 1, cruralpha);

        registerInternalCru(0x36, 1, crurCassetteIn);
    }
    
	/**
	 * @param addr
	 */
	protected synchronized void checkKeyscanPattern(int addr) {
		
		long now = System.currentTimeMillis();
		
		// always favor this one
		if (addr == 1) {
			// use this
			knownKeyscanPattern.clear();
			knownKeyscanPattern.add(1);
			
			keyscanPattern.clear();
			getMachine().getKeyboardHandler().resetProbe();
			
			nextKeyscanPatternCheckTime = now + 1000;
			return;
		}
		else if (now < nextKeyscanPatternCheckTime) {
			// ignore
			return;
		}
		
		// we haven't seen the expected pattern in a while
		if (!knownKeyscanPattern.isEmpty()) {
			if (knownKeyscanPattern.indexOf(addr) == keyscanPattern.size()) {
				keyscanPattern.add(addr);
				if (keyscanPattern.size() == knownKeyscanPattern.size()) {
					keyscanPattern.clear();
					getMachine().getKeyboardHandler().resetProbe();
				}
				return;
			} else {
				// mismatch
				knownKeyscanPattern.clear();
			}
		}

		keyscanPattern.add(addr);
		
		// check for a repeat
		if (keyscanPattern.contains(3) && keyscanPattern.contains(4)) {
			for (int seqlen = 1; seqlen < keyscanPattern.size() / 2; seqlen++) {
				int lastSeq = keyscanPattern.size() - seqlen;
				List<Integer> seq = new ArrayList<Integer>(keyscanPattern.subList(lastSeq, lastSeq + seqlen)); 
				if (Collections.lastIndexOfSubList(keyscanPattern.subList(0, lastSeq), seq) == lastSeq - seqlen) {
					// found the sequence: now, bias toward the smallest address
					List<Integer> expSeq = new ArrayList<Integer>(seq);
					int min = Integer.MAX_VALUE;
					int minI = 0;
					for (int i = 0; i < seqlen; i++) {
						int v = seq.get(i);
						if (v < min) {
							expSeq.clear();
							expSeq.addAll(keyscanPattern.subList(lastSeq - i, lastSeq - i + seqlen));
							minI = i;
							min = v;
						}
					}

					knownKeyscanPattern.clear();
					knownKeyscanPattern.addAll(expSeq);
					
					keyscanPattern.clear();
					// add entries we skipped
					keyscanPattern.addAll(knownKeyscanPattern.subList(0, minI));

					getMachine().getKeyboardHandler().resetProbe();
					
					nextKeyscanPatternCheckTime = now + 1000;
					return;
				}
			}
		}
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

	/* (non-Javadoc)
	 * @see v9t9.engine.hardware.BaseCruChip#resetClock()
	 */
	@Override
	protected void resetClock() {
		super.resetClock();
//		getMachine().getSound().getCassetteVoice().setClock(
//				(float) clockRegister * 64 / getMachine().getCpu().getBaseCyclesPerSec());

	}
    /** Access the registration object for CRU handlers */
    public CruManager getCruManager() {
    	return manager;
    }
}
