/*
 * (c) Ed Swartz, 2010
 *
 */
package v9t9.emulator.hardware;

import java.util.ArrayList;
import java.util.List;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.IMemoryIOHandler;
import v9t9.emulator.runtime.cpu.CpuF99b;
import v9t9.keyboard.KeyboardState;

/**
 * CRU handlers for the F99 machine.
 * @author ejs
 */
public class InternalCruF99 extends BaseCruAccess {

	public final static int CRU_BASE = 0x80;
	
	/** enabled interrupts (r/w) */
	public final static int INTS = CRU_BASE + 0;
	/** pending interrupts (r) */
	public final static int INTSP = CRU_BASE + 1;
	/** keyboard column (w) or bits (r) */
	public final static int KBD = CRU_BASE + 2;
	/** keyboard alpha */
	public final static int KBDA = CRU_BASE + 3;
	/** audio gate */
	public final static int GATE = CRU_BASE + 4;
	/** floppy controller */
	public static final int DISK_BASE = CRU_BASE + 8;	// 8, 9, 10, 11, 12, 13
	
	protected List<IMemoryIOHandler> ioHandlers = new ArrayList<IMemoryIOHandler>();
	
	/**
	 * @param machine
	 * @param keyboardState
	 */
	public InternalCruF99(Machine machine, KeyboardState keyboardState) {
		super(machine, keyboardState, 8);
		
    	intExt = -1;
    	intVdp = CpuF99b.INT_VDP;
    	intClock = -1;

	}
	
	public void handleWrite(int addr, byte val) {
		switch (addr) {
		case INTS:
			enabledIntMask = val;
			break;
			
		case INTSP:
			for (int i = 0; i < 8; i++) {
				int mask = (1 << i);
				if ((val & mask) == 0) {
					if ((currentints & mask) != 0)
						acknowledgeInterrupt(i);
				}
				if (mask == (1 << CpuF99b.INT_KBD)) {
					keyboardState.resetProbe();
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
			machine.getSound().setAudioGate(addr, val != 0);
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
		switch (addr) {
		case INTS:
			return (byte) (enabledIntMask);
		case INTSP:
			return (byte) (currentints & enabledIntMask);
			
		case KBD: {
			int alphamask = 0;
			
			//if (!alphaLockMask)
			//	alphamask = keyboardState.getAlpha() ? 0x10 : 0x0;
			
			int col = keyboardState.getKeyboardRow(crukeyboardcol);
			return (byte) (col | alphamask);
		}
		
		case KBDA:
			keyboardState.setProbe();
			return (byte) (keyboardState.getAlpha() ? 1 : 0);
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
}
