/*
  InternalCruF99.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.machine;

import static v9t9.common.keyboard.KeyboardConstants.MASK_CAPS_LOCK;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import v9t9.common.machine.IMachine;
import v9t9.engine.dsr.IMemoryIOHandler;
import v9t9.engine.hardware.BaseCruChip;
import v9t9.machine.f99b.cpu.CpuF99b;

/**
 * CRU handlers for the F99 machine.
 * @author ejs
 */
public class InternalCruF99 extends BaseCruChip {

	/** enabled interrupts (r/w) */
	public final static int INTS = 0;
	/** pending interrupts (r) */
	private final static int INTSP = 1;
	/** keyboard column (w) or bits (r) */
	private final static int KBD = 2;
	/** keyboard alpha */
	private final static int KBDA = 3;
	/** audio gate */
	private final static int GATE = 4;
	/** random number */
	private final static int RND = 7;
	/** floppy controller */
	public static final int DISK_BASE = 8;	// 8, 9, 10, 11, 12, 13
	
	protected List<IMemoryIOHandler> ioHandlers = new ArrayList<IMemoryIOHandler>();

	private Random random;

	private int cruBase;
	
	/**
	 * @param machine
	 * @param keyboardState
	 */
	public InternalCruF99(IMachine machine, int cruBase) {
		super(machine, 8);
		this.cruBase = cruBase;
		
    	intExt = -1;
    	intVdp = CpuF99b.INT_VDP;
    	intClock = -1;

    	random = new Random(0);
	}
	
	public void handleWrite(int addr, byte val) {
		switch (addr - cruBase) {
		case INTS:
			enabledIntMask = val;
			break;
			
		case INTSP:
			for (int i = 0; i < 8; i++) {
				int mask = (1 << i);
				if ((val & mask) == 0) {
					if ((currentInts & mask) != 0)
						acknowledgeInterrupt(i);
					if (mask == (1 << CpuF99b.INT_KBD)) {
						getMachine().getKeyboardHandler().resetProbe();
					}
				}
				else {
					if (mask == (1 << CpuF99b.INT_KBD) && getMachine().getKeyboardHandler().anyKeyAvailable()) {
						getMachine().getKeyboardHandler().resetProbe();
					}

				}
			}
			break;
			
		case KBD:
			crukeyboardcol = val & 0x7;
			break;
			
		case KBDA:
			alphaLockMask = (val & 0x80) != 0;
			break;
			
		case GATE:
			getMachine().getSound().setAudioGate(addr, val != 0);
			break;

		case RND:
			random.setSeed(val);
			break;

		default:
			for (IMemoryIOHandler handler : ioHandlers) {
				if (handler.handlesAddress(addr)) {
					handler.writeData(addr, val);
					break;
				}
			}
			break;
		}
	}
	
	public byte handleRead(int addr) {
		switch (addr - cruBase) {
		case INTS:
			return (byte) (enabledIntMask);
		case INTSP:
			//return (byte) (currentInts & enabledIntMask);
			return (byte) (currentInts & enabledIntMask);
			
		case KBD: {
			int alphamask = 0;
			
			//if (!alphaLockMask)
			//	alphamask = keyboardState.getAlpha() ? 0x10 : 0x0;
			
			int col = getMachine().getKeyboardState().getKeyboardRow(crukeyboardcol);
			return (byte) (col | alphamask);
		}
		
		case KBDA:
			getMachine().getKeyboardHandler().setProbe();
			boolean isCaps = (getMachine().getKeyboardState().getLockMask() & MASK_CAPS_LOCK) != 0;
			return (byte) (isCaps ? 1 : 0);
			
		case RND:
			return (byte) random.nextInt(256);
			
		default:
			for (IMemoryIOHandler handler : ioHandlers) {
				if (handler.handlesAddress(addr)) {
					return handler.readData(addr);
				}
			}
			break;
		}
		
		return 0;
	}

	/**
	 * @param memoryDiskDsr
	 */
	public void addIOHandler(IMemoryIOHandler ioHandler) {
		ioHandlers.add(ioHandler);
	}

	/**
	 * @return
	 */
	public int getCruBase() {
		return cruBase;
	}
}
