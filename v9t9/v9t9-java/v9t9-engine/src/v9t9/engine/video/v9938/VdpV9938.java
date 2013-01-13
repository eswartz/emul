/**
 * 
 */
package v9t9.engine.video.v9938;


import static v9t9.common.hardware.VdpTMS9918AConsts.*;
import static v9t9.common.hardware.VdpV9938Consts.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.ejs.gui.images.V99ColorMapUtils;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.hardware.VdpV9938Consts;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;

/**
 * V9938 video chip support.  This functions as a superset of the TMS9918A.
 * TODO: acceleration: YMMV, SRCH, test HMMM and LMMM; set registers to expected values when done
 * @author ejs  
 *
 */
public class VdpV9938 extends VdpTMS9918A implements IVdpV9938 {

	private final static Map<Integer, String> regNames9938 = new HashMap<Integer, String>();
	private final static Map<String, Integer> regIds9938 = new HashMap<String, Integer>();
	
	private static void register(int reg, String id) {
		//System.out.println(reg + " = " + id);
		regNames9938.put(reg, id);
		regIds9938.put(id, reg);
	}
	
	static {
		register(REG_ST, "ST");
		
		for (int i = 0; i < 48; i++) {
			register(i, "VR" + i);		// consistent naming with TMS9918A, 
										// even though there is a change from 1 to 2 digits 
		}
		for (int i = 0; i < 9; i++) {
			register(i + VdpV9938Consts.REG_SR0, "SR" + i);
		}
		for (int i = 0; i < 16; i++) {
			register(i + VdpV9938Consts.REG_PAL0, "PAL" + (i < 10 ? "0" : "") + i);
		}
	}
	
	static final byte[][] defaultPalette = {
		{ 0, 0, 0 }, 
		{ 0, 0, 0 }, 
		{ 6, 1, 1 }, 
		{ 7, 3, 3 }, 
		{ 1, 1, 7 }, 
		{ 3, 2, 7 }, 
		{ 1, 5, 1 }, 
		{ 6, 2, 7 }, 
		{ 1, 7, 1 }, 
		{ 3, 7, 3 }, 
		{ 6, 6, 1 }, 
		{ 6, 6, 4 }, 
		{ 4, 4, 1 }, 
		{ 2, 6, 5 }, 
		{ 5, 5, 5 }, 
		{ 7, 7, 7 },
	};


	// the MSX 2 speed is 21 MHz (21477270 hz)
	
	static public final int CLOCK_RATE = 21477270;
	
	// cycles for commands execute in 3579545 Hz (from blueMSX)
	/* 3579545 / 60 target # cycles to be executed per tick */
    static public final SettingSchema settingMsxClockDivisor = new SettingSchema(
    		ISettingsHandler.MACHINE,
    		"MsxClockDivisor", new Integer(6));

	/* from mame and blueMSX:
	 * 
     */
	final static int command_cycles[][] = {
			// SRCH x6
			{ 92, 125, 92, 92 },
			// LINE x7
			{ 120, 147, 120, 120 },
			// LMMV x8
			{ 98, 137, 98, 124 },
			// LMMM x9
			{ 129, 197, 129, 132 },
			// LMCM xA
			{ 129, 197, 129, 132 },
			// LMMC xB
			{ 129, 197, 129, 132 },
			// HMMV xC
			{ 49, 65, 49, 62 },
			// HMMM xD
			{ 92, 136, 92, 97 },
			// YMMM xE
			{ 65, 125, 65, 68 },
			// HMMC xF
			{ 49, 65, 49, 62 },
	};
    
	private short[] palette;
	private boolean palettelatched;
	private byte palettelatch;
	private int statusidx;
	private byte[] statusvec = new byte[9];
	private int indreg;
	private boolean indinc;
	private boolean isEnhancedMode;

	/** ms */
	private int blinkPeriod;
	/** ms */
	private int blinkOnPeriod;
	/** ms */
	private int blinkOffPeriod;
	
	private int rowstride;	// stride in bytes from row to row in modes 4-7
	private int pixperbyte;	// pixels per byte
	private int pixshift;	// shift in bits from an X coord to the colors for that bit
	private int pixmask;	// mask from a byte to a color
	


	private int currentcycles = 0; // current cycles left
	private IProperty msxClockDivisor;

	/** Working variables for command execution */
	static class CommandVars {
		int dx, dy;
		int sx, sy;
		/** nx ny, or maj/min for lines */
		int nx, ny;
		/** command, masked */
		byte cmd;
		/** tell if we handle S2_TR */
		boolean isDataMoveCommand;
		/** argument  */
		byte arg;
		/** color byte */
		byte clr;
		/** operation, masked */
		byte op;
		/** line step fraction: 16.16 */
		int frac;
		/** countdown */
		int cnt;
		int ycnt;
		public int dix;
		public int diy;
		/** destination memory select */
		int mxd;
		/** sourcememory select */
		int mxs;
		/** cycles used per op */
		int cycleUse;
	}
	
	private CommandVars cmdState = new CommandVars();
	private IProperty demoPlaying;

	private ListenerList<IAccelListener> accelListeners = new ListenerList<IVdpV9938.IAccelListener>();
	private IProperty realTime;
	

	public VdpV9938(IMachine machine) {
		super(machine);
		
		msxClockDivisor = Settings.get(machine, settingMsxClockDivisor); 
		demoPlaying = Settings.get(machine, IDemoHandler.settingPlayingDemo);
		realTime = Settings.get(machine, ICpu.settingRealTime);
		
//		machine.getDemoManager().registerActor(new VdpV9938DataDemoActor());
//		machine.getDemoManager().registerActor(new VdpV9938AccelDemoActor());
	}

	protected byte[] allocVdpRegs() {
		return new byte[48];
	}
	
	@Override
	public void initRegisters() {
		super.initRegisters();
		
		this.palette = new short[16];

		for (int i = 0; i < 16; i++) {
			byte[] p = defaultPalette[i];
			palette[i] = V99ColorMapUtils.rgb8ToRgbRBXG(V99ColorMapUtils.getGRB333(
					p[0], p[1], p[2]));
		}
		
		// color burst regs(pg 149)
		vdpregs[20] = 0;
		vdpregs[21] = 0x3b;
		vdpregs[22] = 0x05;
	}
	
	public void writeRegisterIndirect(byte val) {
		if (indreg != 17) {
			setRegister(indreg, val);
		}
		if (indinc) {
			indreg = (indreg + 1) & 0x3f;
		}
	}

	protected void loadVdpReg(int num, byte val) {
		if (num != 44 && num != 46) {
			setRegister(num, val);
		}
		else {
			// don't fire
			vdpregs[num] = val;
		}
	}

	private void switchBank() {
		BankedMemoryEntry memoryBank = getVdpMmio().getMemoryBank();
		if (memoryBank == null)
			return;
		
		int vdpbank = (vdpregs[14] & 0x7);
		
		// 16k mode?
		if (/*(vdpregs[8] & R8_VR) == 0 ||*/ memoryBank.getBankCount() == 1) {
			vdpbank = 0;
		} else {
			// expansion RAM?
			if ((vdpregs[45] & R45_MXC) != 0 && memoryBank.getBankCount() >= 8) {
				vdpbank = (vdpbank & 0x3) | 0x8;
			}
		}
		
		memoryBank.selectBank(vdpbank);
		//System.out.println("-->vdpbank " + vdpbank);
	}

	public void writeColorData(byte val) {
		if (!palettelatched) {
			palettelatch = val;
			palettelatched = true;
		} else {
			// first byte: red red/blue, second: green
			final int col = vdpregs[16] & 0xf;
			palette[col] = (short) ((palettelatch << 8) | (val & 0xff));
			
			fireRegisterChanged(col + REG_PAL0, palette[col]);
			
			vdpregs[16] = (byte) ((vdpregs[16]+1)&0xf);
			fireRegisterChanged(16, vdpregs[16]);
			
			palettelatched = false;
		}
	}

	
	/* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#readVdpStatus()
     */
    public byte readVdpStatus() {
    	if (statusidx == 0)
    		return super.readVdpStatus();
    	
    	byte ret = statusvec[statusidx];
        return ret;
    }

	public boolean isEnhancedMode() {
		return isEnhancedMode;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#doTick()
	 */
	@Override
	protected void doTick() {
		super.doTick();

		int targetRate = CLOCK_RATE / msxClockDivisor.getInt() / vdpInterruptRate.getInt();
		
		if (/*!Cpu.settingRealTime.getBoolean() ||*/ currentcycles < 0)
			currentcycles += targetRate;
		else
			currentcycles = targetRate;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpV9938#addAccelListener(v9t9.common.hardware.IVdpV9938.IAccelListener)
	 */
	@Override
	public void addAccelListener(IAccelListener listener) {
		accelListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpV9938#removeAccelListener(v9t9.common.hardware.IVdpV9938.IAccelListener)
	 */
	@Override
	public void removeAccelListener(IAccelListener listener) {
		accelListeners.remove(listener);
	}

	@Override
	public boolean isThrottled() {
		// this is called by normal execution to ensure the acceleration isn't too fast
		return !isAccelActive() || (currentcycles <= 0 && realTime.getBoolean()) || (machine.isPaused() && !demoPlaying.getBoolean());
	}
	
	@Override
	public synchronized void work() {
		if (!isThrottled()) {
			// this should be called only after validating !isThrottled(), except by demos
			if (!accelListeners.isEmpty()) {
				for (Object obj : accelListeners.toArray()) {
					((IAccelListener) obj).accelCommandWork();
				}
			}
			handleCommand();
		}
	}

	public boolean isAccelActive() {
		return (statusvec[2] & S2_CE) != 0;
	}
	
	private void setAccelActive(boolean flag) {
		if (flag) {
			statusvec[2] |= S2_CE;
			
			if (!accelListeners.isEmpty()) {
				for (Object obj : accelListeners.toArray()) {
					((IAccelListener) obj).accelCommandStarted();
				}
			}
		} else {
			statusvec[2] &= ~S2_CE;
			cmdState.cmd = 0;
			cmdState.isDataMoveCommand = false;
			log("MSX command done");
			
			if (!accelListeners.isEmpty()) {
				for (Object obj : accelListeners.toArray()) {
					((IAccelListener) obj).accelCommandEnded();
				}
			}
		}
	}

	private void setupCommand() {
		byte val = vdpregs[46];
		cmdState.cmd = (byte) (val & R46_CMD_MASK);
		cmdState.op = (byte) (val & R46_LOGOP_MASK);
		cmdState.clr = vdpregs[R44_CLR];
		cmdState.dx = readDX();
		cmdState.dy = readDY();
		cmdState.arg = vdpregs[R45_ARG];
		cmdState.dix = (cmdState.arg & R45_DIX) == 0 ? 1 : -1; 
		cmdState.diy = (cmdState.arg & R45_DIY) == 0 ? 1 : -1;
		cmdState.mxs = cmdState.arg & R45_MXS;
		cmdState.mxd = cmdState.arg & R45_MXD;
		cmdState.cnt = cmdState.ycnt = 0;
		if (cmdState.cmd == R46_CMD_LINE) {
			// in line mode, X/Y are biased into 16:16
			int maj = readMaj();
			int min = readMin();
			if (dumpFullInstructions.getBoolean() && dumpVdpAccess.getBoolean())
				log("Line: x="+cmdState.dx+",y="+cmdState.dy+",dix="+cmdState.dix+",diy="+cmdState.diy
						+",maj="+maj+",min="+min+",axis="+(vdpregs[45]&R45_MAJ));
			int frac = maj != 0 ? min * 0x10000 / maj : 0;
			if ((vdpregs[45] & R45_MAJ) == 0) {
				cmdState.nx = 0x10000 * cmdState.dix;
				cmdState.ny = frac * cmdState.diy;
			} else {
				cmdState.nx = frac * cmdState.dix;
				cmdState.ny = 0x10000 * cmdState.diy;
			}
			cmdState.dx <<= 16;
			cmdState.dy <<= 16;
			cmdState.cnt = maj;
		} else {
			cmdState.nx = readNX();
			cmdState.ny = readNY();
		}
		
		if (cmdState.cmd == R46_CMD_HMMC
				|| cmdState.cmd == R46_CMD_HMMM
				|| cmdState.cmd == R46_CMD_HMMV) {
			cmdState.op = R46_LOGOP_INP;
			
			// the high-speed moves are aligned to bytes
			cmdState.sx -= cmdState.sx % pixperbyte;
			cmdState.dx -= cmdState.dx % pixperbyte;
			cmdState.nx -= cmdState.nx % pixperbyte;
			
			cmdState.isDataMoveCommand = (cmdState.cmd == R46_CMD_HMMC); 
		} else if (cmdState.cmd == R46_CMD_LMMC
				|| cmdState.cmd == R46_CMD_LMMM
				|| cmdState.cmd == R46_CMD_LMMV) {
			
			cmdState.isDataMoveCommand = (cmdState.cmd == R46_CMD_LMMC);
		}

		// from MAME
		int cmdIdx = (cmdState.cmd - R46_CMD_SRCH) >> 4;
		if (cmdIdx >= 0 && cmdIdx < command_cycles.length) {
			//int modeSelect = ((vdpregs[1]>>6)&1)|(vdpregs[8]&2)|((vdpregs[9]<<1)&4);
			int modeSelect = (((vdpregs[1] >> 6)&1) | (vdpregs[8] & 2));
			cmdState.cycleUse = command_cycles[cmdIdx][modeSelect];
		} else {
			cmdState.cycleUse = 0;
		}
		
		// all clear
		statusvec[2] = 0;
		
		if (dumpFullInstructions.getBoolean() && dumpVdpAccess.getBoolean())
			log("MSX command " + HexUtils.toHex2(cmdState.cmd)
					+ " arg=" + HexUtils.toHex2(cmdState.arg) 
					+ " op=" + HexUtils.toHex2(cmdState.op)
				+ " clr=" + HexUtils.toHex2(cmdState.clr)
				+ " DX,DY= " + cmdState.dx + "," + cmdState.dy +"; NX,NY=" + cmdState.nx +","+ cmdState.ny);
	}
	/**
	 * Do some work of the acceleration command.
	 * We execute one cycle of the command each tick.
	 */
	private void handleCommand() {
		int addr, saddr, daddr;
		
		if (pixperbyte == 0) {
			// not a supported mode
			setAccelActive(false);
			return;
		}
		
		switch (cmdState.cmd) {
		case R46_CMD_STOP:
			setAccelActive(false);
			return;
			
		case R46_CMD_PSET:
			// instantaneous!
			setPixel(cmdState.dx, cmdState.dy, cmdState.clr & pixmask, cmdState.op);
			setAccelActive(false);
			break;
			
		case R46_CMD_POINT:
			// instantaneous!
			byte pixel = getPixel(cmdState.dx, cmdState.dy);
			statusvec[7] = pixel;
			setAccelActive(false);
			break;
			
		case R46_CMD_LINE:
			while ((currentcycles -= cmdState.cycleUse) > 0) {
				setPixel(cmdState.dx >> 16, cmdState.dy >> 16, 
					cmdState.clr, cmdState.op);
				cmdState.dx += cmdState.nx;
				cmdState.dy += cmdState.ny;
				if (cmdState.cnt-- <= 0) {
					setAccelActive(false);
					break;
				}
			}
			break;
			
			// send a series of CLR bytes to rectangle
		case R46_CMD_HMMC:
			if ((statusvec[2] & S2_TR) == 0) {
				// always ready for next byte
				statusvec[2] |= S2_TR;
				addr = getAbsAddr(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy, cmdState.mxd);
				//System.out.println(cmdState.cnt+","+cmdState.ycnt+","+addr);
				vdpMmio.writeFlatMemory(addr, vdpregs[R44_CLR]);
				cmdState.cnt += pixperbyte;
				if (cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						statusvec[2] &= ~S2_TR;
					}
				}
				currentcycles -= cmdState.cycleUse;
			}
			break;

			// send byte rectangle from vram to vram
		case R46_CMD_HMMM:
			while ((currentcycles -= cmdState.cycleUse) > 0) {
				saddr = getAbsAddr(cmdState.sx + cmdState.cnt * cmdState.dix, 
						cmdState.sy + cmdState.ycnt * cmdState.diy, cmdState.mxs);
				daddr = getAbsAddr(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy, cmdState.mxd);
				vdpMmio.writeFlatMemory(daddr, 
						vdpMmio.readFlatMemory(saddr));
				cmdState.cnt += pixperbyte;
				if (cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						break;
					}
				}
			}
			break;

			// send single CLR byte to rectangle
		case R46_CMD_HMMV:
			while ((currentcycles -= cmdState.cycleUse) > 0) {
				addr = getAbsAddr(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy, cmdState.mxd);
				vdpMmio.writeFlatMemory(addr, cmdState.clr);
				cmdState.cnt += pixperbyte;
				if (cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						break;
					}
				}
			}
			break;
			
			// send series of CLR pixels to rectangle 
		case R46_CMD_LMMC:
			if ((statusvec[2] & S2_TR) == 0) {
				// always ready for next byte
				statusvec[2] |= S2_TR;
				setPixel(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy,
						vdpregs[R44_CLR] & pixmask, cmdState.op);
				currentcycles -= cmdState.cycleUse;
				if (++cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						statusvec[2] &= ~S2_TR;
					}
				}
			}
			break;			
			
			// send single CLR pixel to rectangle 
		case R46_CMD_LMMV:
			while ((currentcycles -= cmdState.cycleUse) > 0) {
				setPixel(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy,
						cmdState.clr & pixmask, cmdState.op);
				if (++cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						break;
					}
				}
			}
			break;
			
			// send pixel rectangle from vram to vram
		case R46_CMD_LMMM:
			while ((currentcycles -= cmdState.cycleUse) > 0) {
				byte color = getPixel(cmdState.sx + cmdState.cnt * cmdState.dix, 
						cmdState.sy + cmdState.ycnt * cmdState.diy);
				setPixel(cmdState.dx + cmdState.cnt * cmdState.dix, 
						cmdState.dy + cmdState.ycnt * cmdState.diy, cmdState.op, color);
				cmdState.cnt += pixperbyte;
				if (cmdState.cnt > cmdState.nx) {
					cmdState.cnt = 0;
					if (++cmdState.ycnt > cmdState.ny) {
						setAccelActive(false);
						break;
					}
				}
			}
			break;
			
		}
		
		
	}

	/**
	 * Get the absolute address from these absolute pixels
	 * @param x
	 * @param y
	 * @return
	 */
	private int getAbsAddr(int x, int y, int ramSelect) {
		if (ramSelect == 0)
			return (y * rowstride + (x / pixperbyte)) & 0x1ffff;
		else
			return ((y * rowstride + (x / pixperbyte)) & 0xffff) + 0x20000;
	}
	
	/**
	 * Get a logical pixel
	 *
	 * @param x
	 * @param y
	 */
	private byte getPixel(int x, int y) {
		x &= 0x1ff;
		y &= 0x3ff;
		int addr = getAbsAddr(x, y, cmdState.mxs);
		byte current = vdpMmio.readFlatMemory(addr);
		int xmod = pixperbyte != 0 ? pixperbyte - 1 - x % pixperbyte : 0; 
		int xshift = (xmod * pixshift);
		
		byte mask = (byte) (pixmask << xshift);
		
		return (byte) ((current & mask) >> xshift); 
	}

	/**
	 * Set a logical pixel
	 *
	 * @param x
	 * @param y
	 * @param color
	 */
	private void setPixel(int x, int y, int color, int op) {
		x &= 0x1ff;
		y &= 0x3ff;
		int addr = getAbsAddr(x, y, cmdState.mxd);
		byte current = vdpMmio.readFlatMemory(addr);
		int xmod = pixperbyte != 0 ? pixperbyte - 1 - x % pixperbyte : 0; 
		int xshift = (xmod * pixshift);
		byte updated = current;
		
		byte mask = (byte) (pixmask << xshift);
		color = (color & pixmask) << xshift;
		
		// test if color is non-blank for TEST operations
		if ((op & 0x8) != 0 && color == 0)
			return;
			
		switch (op) {
			
		case R46_LOGOP_TIMF:
		case R46_LOGOP_INP:
			updated = (byte) ((current & ~mask) | color);
			break;
		case R46_LOGOP_TOR:
		case R46_LOGOP_OR:
			updated = (byte) (current | color);
			break;
		case R46_LOGOP_TAND:
		case R46_LOGOP_AND:
			updated = (byte) (current & color);
			break;
		case R46_LOGOP_TEOR:
		case R46_LOGOP_EOR:
			updated = (byte) (current ^ color);
			break;
		case R46_LOGOP_TNOT:
		case R46_LOGOP_NOT:
			updated = (byte) ((current & ~mask) | (color ^ pixmask));
			break;
		}
		
		vdpMmio.writeFlatMemory(addr, updated);  
	}

	private int readDY() {
		return (vdpregs[R38_DY_LO] & 0xff) | ((vdpregs[R39_DY_HI] & 0x3) << 8);
	}

	private int readDX() {
		return (vdpregs[R36_DX_LO] & 0xff) | ((vdpregs[R37_DX_HI] & 0x1) << 8);
	}
	
	private int readNY() {
		return (vdpregs[R42_NY_LO] & 0xff) | ((vdpregs[R43_NY_HI] & 0x3) << 8);
	}
	
	private int readNX() {
		return (vdpregs[R40_NX_LO] & 0xff) | ((vdpregs[R41_NX_HI] & 0x1) << 8);
	}
	
	/** Note: swap the # of bits wrt NX and NY */
	private int readMin() {
		return (vdpregs[R42_NY_LO] & 0xff) | ((vdpregs[R43_NY_HI] & 0x1) << 8);
	}
	
	private int readMaj() {
		return (vdpregs[R40_NX_LO] & 0xff) | ((vdpregs[R41_NX_HI] & 0x3) << 8);
	}


	/**
	 * Tell whether interlacing is active.
	 * 
	 * For use in rendering, we need to know whether raw R9_IL (interlace) bit is set
	 * and also the R9_EO (even/odd) bit is set, which would provide the page flipping
	 * required to *see* two pages.  Finally, the "odd" graphics page must be visible
	 * for the flipping and interlacing to occur.
	 * @return
	 */
	
	public boolean isInterlacedEvenOdd() {
		return (vdpregs[9] & R9_EO + R9_IL) == R9_EO + R9_IL 
			&& (vdpregs[2] & 0x20) != 0;

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#saveState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		super.saveState(section);

		ISettingSection palettes = section.addSection("Palette");
		for (int i = 0; i < palette.length; i++)
			palettes.put(HexUtils.toHex2(i), HexUtils.toHex4(palette[i]));

		ISettingSection statuses = section.addSection("Statuses");
		for (int i = 0; i < statusvec.length; i++)
			statuses.put(HexUtils.toHex2(i), HexUtils.toHex2(statusvec[i]));
		
		section.put("AccelActive", isAccelActive());
		
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#loadState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		super.loadState(section);
		
		if (section == null)
			return;

		setAccelActive(section.getBoolean("AccelActive"));
		if (isAccelActive()) {
			setupCommand();
		}

		ISettingSection palettes = section.getSection("Palette");
		if (palettes != null) {
			for (String name : palettes.getSettingNames()) {
				try {
					int idx = Integer.parseInt(name, 16);
					if (idx >= 0 && idx < palette.length)
						setRegister(VdpV9938Consts.REG_PAL0 + idx, 
							(short) Integer.parseInt(palettes.get(name), 16));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		ISettingSection statuses = section.getSection("Statuses");
		if (statuses != null) {
			for (String name : statuses.getSettingNames()) {
				try {
					int idx = Integer.parseInt(name, 16);
					if (idx >= 0 && idx < statusvec.length)
						statusvec[idx] = 
							(byte) Integer.parseInt(statuses.get(name), 16);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

    public String getGroupName() {
    	return "VDP V9938 Registers";
    }

    
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return REG_COUNT;
	}


	protected String getRegisterId(int reg) {
		return regNames9938.get(reg);
	}

	@Override
	public int getRegisterNumber(String id) {
		Integer num = regIds9938.get(id);
		return num != null ? num : Integer.MIN_VALUE;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg >= getRegisterCount())
			return 0;
		if (reg >= VdpV9938Consts.REG_PAL0)
			return palette[reg - VdpV9938Consts.REG_PAL0];
		else if (reg >= VdpV9938Consts.REG_SR0)
			return statusvec[reg - VdpV9938Consts.REG_SR0];
		else
			return super.getRegister(reg);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#setRegister(int, byte)
	 */
	@Override
	public int setRegister(int reg, int value) {
		if (reg >= REG_PAL0) {
			if (reg >= REG_PAL0 + palette.length)
				return 0;
			
			final int color = reg - VdpV9938Consts.REG_PAL0;
			int old = palette[color] & 0xffff;
			palette[color] = (short) value;
			
			if (dumpFullInstructions.getBoolean() && dumpVdpAccess.getBoolean())
				log("palette register " + color + " " + HexUtils.toHex2(old) + " -> " + HexUtils.toHex2(value));

			fireRegisterChanged(reg, value);
			return old;
		} 
		if (reg >= VdpV9938Consts.REG_SR0) {
			int old = statusvec[reg - VdpV9938Consts.REG_SR0] & 0xff;
			statusvec[reg - VdpV9938Consts.REG_SR0] = (byte) value;
		
			fireRegisterChanged(reg, (byte) value);
			
			return old;
		} else {
			return super.setRegister(reg, value);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#doSetVdpReg(int, byte, byte)
	 */
	@Override
	protected void doSetVdpReg(int reg, byte old, byte val) {
		switch (reg) {
		case 8:
			switchBank();
			break;
		case 13:
			// blinking period (pg 6)
			blinkOnPeriod = ((val >> 4) & 0xf) * 1000 / 6;
			blinkOffPeriod = (val & 0xf) * 1000 / 6;
			blinkPeriod = blinkOnPeriod + blinkOffPeriod;
			break;
		case 14:
			if (old != val)
				switchBank();
			break;
		case 15:
			// status register pointer
			statusidx = val & 0xf;
			break;
		case 16:
			// palette #
			palettelatched = false;
			break;
		case 17:
			// indirect register access: sets the next register whose value
			// can be set through port 3 (though, reg 17 itself will not be modified)
			indreg = val & 0x3f;
			indinc = (val & 0x80) == 0;
			break;
			
		case 19:
			// set to the line # on which to generate an interrupt (?)
			// R0 bit 4 enables
			// http://map.tni.nl/articles/split_guide.php
			break;
			
		case 32: // sx lo
		case 33: // sx hi (2)
		case 34: // sx lo	   or sy lo
		case 35: // sx hi (2)  or sy hi (1)
		case 36: // dx lo
		case 37: // dx hi (2)
		case 38: // dy lo
		case 39: // dy hi (1)
		case 40: // nx lo
		case 41: // nx hi (2)
		case 42: // ny lo
		case 43: // ny hi (1)
			break;
		case 44: // CLR (data to transfer)
			if (isAccelActive() && cmdState.isDataMoveCommand) {
				// got the next byte
				statusvec[2] &= ~S2_TR;
				work();
			}
			break;
		case 45: // ARG
			if (((old ^ val) & R45_MXC) != 0) {
				// doesn't affect video rendering
				switchBank();
			}
			break;
		case 46: // CMD
			if ((statusvec[2] & S2_CE) == 0 && pixperbyte != 0) {
				setupCommand();
				setAccelActive(true);
				work();
			}
			//}
			break;
		default:
			super.doSetVdpReg(reg, old, val);
			break;
		}
		
	}
	

	protected String getStatusString(byte s) {
		return caten(yOrN("Int", s & 0x80),
				yOrN("9 Sprites", s & 0x40),
				yOrN("Coinc", s & 0x20))
				+ " | 9th: " + (s & 0x1f);
	}
	
	@Override
	protected String getRegisterName(int reg) {
		switch (reg) {
		case 10:
			return "Color Table High";
		case 11:
			return "Sprite Table High";
		case 12:
			return "Blink Color";
		case 13:
			return "Blink Period";
		case 14:
			return "Page Base";
		case 15:
			return "Status Register #";
		case 16:
			return "Color Palette Address";
		case 17:
			return "Control Register Pointer";
		case 18:
			return "Display Adjust";
		case 19:
			return "Interrupt line";
		case 23:
			return "Display offset";
		case 20:
			return "Color burst 1";
		case 21:
			return "Color burst 2";
		case 22:
			return "Color burst 3";
		case 32:
			return "Command Source X Low";
		case 33:
			return "Command Source X High";
		case 34:
			return "Command Source Y Low";
		case 35:
			return "Command Source Y High";
		case 36:
			return "Command Dest X Low";
		case 37:
			return "Command Dest X High";
		case 38:
			return "Command Dest Y Low";
		case 39:
			return "Command Dest Y High";
		case 40:
			return "Command # Dots X Low";
		case 41:
			return "Command # Dots X High";
		case 42:
			return "Command # Dots Y Low";
		case 43:
			return "Command # Dots Y High";
		case 44:
			return "Command Color";
		case 45:
			return "Command Arguments";
			
		case 46:
			return "Command";
			
		case VdpV9938Consts.REG_SR0: 
			return "Status Register 0";
		case VdpV9938Consts.REG_SR0 + 1:
			return "Status Register 1";
		case VdpV9938Consts.REG_SR0 + 2:
			return "Status Register 2";
		case VdpV9938Consts.REG_SR0 + 3:
			return "Column Register Low";
		case VdpV9938Consts.REG_SR0 + 4:
			return "Column Register High";
		case VdpV9938Consts.REG_SR0 + 5:
			return "Row Register Low";
		case VdpV9938Consts.REG_SR0 + 6:
			return "Row Register High";
		case VdpV9938Consts.REG_SR0 + 7:
			return "Color Register";
		case VdpV9938Consts.REG_SR0 + 8:
			return "Border X Register Low";
		case VdpV9938Consts.REG_SR0 + 9:
			return "Border X Register High";
		}

		if (reg >= VdpV9938Consts.REG_PAL0) {
			return "Palette Color " + (reg - VdpV9938Consts.REG_PAL0);
		}
		if (reg >= 8)
			return null;
		
		return super.getRegisterName(reg);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getRegisterFlags(int)
	 */
	@Override
	protected int getRegisterFlags(int reg) {
		if ((reg >= VdpV9938Consts.REG_SR0 && reg < VdpV9938Consts.REG_SR0 + 9) || (reg >= 32 && reg <= 45))
			return IRegisterAccess.FLAG_VOLATILE + IRegisterAccess.FLAG_ROLE_GENERAL;
		if (reg == 46 || reg == 16 || reg == 17)
			return IRegisterAccess.FLAG_SIDE_EFFECTS + IRegisterAccess.FLAG_ROLE_GENERAL;
		return super.getRegisterFlags(reg);
	}

	protected int getRegisterSize(int reg) {
		return reg >= VdpV9938Consts.REG_PAL0 ? 2 : super.getRegisterSize(reg);
	}

	public String getModeName() {
		switch (getModeNumber()) {
		case MODE_GRAPHICS3: return "Graphics 3";
		case MODE_GRAPHICS4: return "Graphics 4";
		case MODE_GRAPHICS5: return "Graphics 5";
		case MODE_GRAPHICS6: return "Graphics 6";
		case MODE_GRAPHICS7: return "Graphics 7";
		case MODE_TEXT2: return "Text 2";
		}
		return super.getModeName();
	}
	

	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		if (reg == REG_ST)
			return getStatusString(vdpStatus);
		short val = 0;
		if (reg >= VdpV9938Consts.REG_PAL0) {
			val = palette[reg - VdpV9938Consts.REG_PAL0];
		} else if (reg >= VdpV9938Consts.REG_SR0) {
			val = statusvec[reg - VdpV9938Consts.REG_SR0];
		} else {
			val = vdpregs[reg];
		}
		switch (reg) {
		case 0:
			return caten(yOrN("DG", val & 0x40), yOrN("IE2", val & 0x40),
					yOrN("IE1", val & 0x20), yOrN("M5", val & R0_M5),
					yOrN("M4", val & R0_M4), yOrN("M3", val & R0_M3))
					+ " (" + getModeName() + ")";
		case 1:
			return caten((val & R1_NOBLANK) != 0 ? "Show" : "Blank",
					yOrN("IE0", val & R1_INT), yOrN("M1", val & R1_M1),
					yOrN("M2", val & R1_M2),
					yOrN("Size 4", val & R1_SPR4), yOrN("Mag", val & R1_SPRMAG))
					+ " (" + getModeName() + ")";
		case 8:
			return caten(yOrN("Mouse", val & 0x80), yOrN("Light Pen", val & 0x40),
					yOrN("NoTransp", val & R8_TP), yOrN("ColorBus", val & 0x10), 
					(val & R8_VR) != 0 ? "64K" : "16K",
					yOrN("Sprites", val & R8_SPD), yOrN("B&W", val & R8_BW));
		case 9:
			return caten((val & R9_LN) != 0 ? "212ln" : "192ln",
					yOrN("Simul1", val & 0x20),
					yOrN("Simul0", val & 0x10),
					yOrN("Interlace", val & R9_IL),
					yOrN("EvenOdd", val & R9_EO),
					(val & 0x02) != 0 ? "PAL" : "NTSC",
					yOrN("DC", val & 0x01));
		case 10:
			return super.getRegisterTooltip(3);
		case 11:
			return super.getRegisterTooltip(6);
		case 12:
			return "BG: " + HexUtils.toHex2(val & 0x7) 
			+ " | FG: " + HexUtils.toHex2((val & 0xf0) >> 4);
		case 13:
			return "On: " + getBlinkOnPeriod() + " ms"
			+ " | Off: " + getBlinkOffPeriod() + " ms";
		case 14:
			return HexUtils.toHex4(val << 14);
		case 15:
			return null;
		case 16:
			return null;
		case 17:
			return yOrN("AutoInc", val & 0x80);
		case 18:
			return "Vert: " + ((val & 0xf0) >> 4)
			+ " | Horiz: " + (val & 0xf);
		case 19:
		case 23:
		case 20:
		case 21:
		case 22:
		case 33:
		case 34:
		case 35:
		case 36:
		case 37:
		case 38:
		case 39:
		case 40:
		case 41:
		case 42:
		case 43:
			return null;
		case 44:
			return "Color High: " + ((val & 0xf0) >> 4) + " | Low: " + (val & 0xf);
		case 45:
			return "Arguments: " +
			caten(yOrN("MXC", val & R45_MXC),
					yOrN("MXD", val & R45_MXD),
					yOrN("MXS", val & R45_MXS),
					yOrN("DIY", val & R45_DIY),
					yOrN("DIX", val & R45_DIX),
					yOrN("EQ", val & R45_EQ),
					yOrN("MAJ", val & R45_MAJ));
		case 46:
			return "Command: " + ((val & 0xf0) >> 4) + " | Lo: " + (val & 0xf);

		case VdpV9938Consts.REG_SR0:
			return getStatusString(vdpStatus);
		case VdpV9938Consts.REG_SR0 + 1:
			return caten(yOrN("FL", val & 0x80),
					yOrN("LPS", val & 0x40),
					"ID: " + ((val & 0x3e) >> 1),
					yOrN("FH", val & 0x01));
		case VdpV9938Consts.REG_SR0 + 2:
			return caten(yOrN("TransRdy", val & 0x80),
					yOrN("VertScan", val & 0x40),
					yOrN("HorizScan", val & 0x20),
					yOrN("BoundDetect", val & 0x10),
					yOrN("EvenOdd", val & 0x02),
					yOrN("CmdExec", val & 0x01)
					);
		}
		
		if (reg >= VdpV9938Consts.REG_SR0)
			return null;
		
		return super.getRegisterTooltip(reg);
	}


	/**
	 * Yields one of the MODE_xxx enums based on the current vdpregs[]
	 * @return
	 */
	public int calculateModeNumber() {
		int reg0 = vdpregs[0] & R0_M3 + R0_M4 + R0_M5;
		int reg1 = vdpregs[1] & R1_M1 + R1_M2;
		
		isEnhancedMode = true;
		
		if (reg1 == 0) {
			if (reg0 == R0_M4)
				return MODE_GRAPHICS3;
			if (reg0 == R0_M3 + R0_M4)
				return MODE_GRAPHICS4;
			if (reg0 == R0_M5)
				return MODE_GRAPHICS5;
			if (reg0 == R0_M3 + R0_M5)
				return MODE_GRAPHICS6;
			if (reg0 == R0_M3 + R0_M4 + R0_M5)
				return MODE_GRAPHICS7;
		} else if (reg1 == R1_M1 && reg0 == R0_M4) {
			return MODE_TEXT2;
		}
		
		isEnhancedMode = false;
		return super.calculateModeNumber();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#isBitmapMode()
	 */
	@Override
	protected boolean isBitmapMode() {
		return super.isBitmapMode() || (modeNumber == MODE_GRAPHICS3);
	}
	
	/** For the V9938, only the on-board 128K is used for display.
	 * Also, when the graphics mode is a standard 9918A mode, the
	 * old masking applies. */
	@Override
	protected int getModeAddressMask() {
		return isEnhancedMode() || (vdpregs[8] & R8_VR) != 0 ? 0x1ffff : 0x3fff;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getScreenTableBase()
	 */
	@Override
	public int getScreenTableBase() {
		switch (getModeNumber()) {
		case MODE_TEXT2:
			return ((vdpregs[2] & 0x7c) * 0x400) & getModeAddressMask();
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			return 0;
		default:
			return super.getScreenTableBase();
		}
	}
	
	 @Override
    public int getScreenTableSize() {
		switch (getModeNumber()) {
		case MODE_TEXT2:
			return 80 * 27; // last 2.5 rows only visible in 212-line mode
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			return 0;
		default:
			return super.getScreenTableSize();
		}
    }
	    
	
	@Override
	public int getSpriteTableBase() {
		return ((((vdpregs[11] & 0x3) << 8) | (vdpregs[5] & 0xff)) << 7) & getModeAddressMask(); 
	}
	
	@Override
	public int getColorTableBase() {
		switch (getModeNumber()) {
		case MODE_BITMAP:
		case MODE_GRAPHICS3:
			return (super.getColorTableBase() | ((vdpregs[10] & 0x7) * 0x4000)) & getModeAddressMask();
		case MODE_TEXT2:
			return (((vdpregs[10] << 8) | (vdpregs[3] & 0xf8)) << 6) & getModeAddressMask();
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			return 0;
		default:
			return ((((vdpregs[10] & 0x7) << 8) | (vdpregs[3] & 0xff)) << 6) & getModeAddressMask();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getColorTableSize()
	 */
	@Override
	public int getColorTableSize() {
		switch (getModeNumber()) {
		case MODE_TEXT2:
			return 2160 / 8;
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			return 0;
		default:
			return super.getColorTableSize();
		}
	}
	
	@Override
	public int getPatternTableBase() {
		switch (getModeNumber()) {
		case MODE_BITMAP:
		case MODE_GRAPHICS3:
			return (super.getPatternTableBase() | ((vdpregs[4] & 0x38) * 0x800)) & getModeAddressMask();
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
			// paging! (A15, A16)
			return ((vdpregs[2] & 0x60) << 10) & getModeAddressMask();
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			 return ((vdpregs[2] & 0x20) << 11) & getModeAddressMask();
		}
		return super.getPatternTableBase();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getPatternTableSize()
	 */
	@Override
	public int getPatternTableSize() {
		switch (getModeNumber()) {
		case MODE_GRAPHICS4:
			return 256 * 212 / 2;
		case MODE_GRAPHICS5:
			return 512 * 212 / 4;
		case MODE_GRAPHICS6:
			return 512 * 212 / 2;
		case MODE_GRAPHICS7:
			return 256 * 212;
		default:
			return super.getPatternTableSize();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpV9938#getBlinkOffPeriod()
	 */
	@Override
	public int getBlinkOffPeriod() {
		return blinkOffPeriod;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpV9938#getBlinkOnPeriod()
	 */
	@Override
	public int getBlinkOnPeriod() {
		return blinkOnPeriod;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpV9938#getBlinkPeriod()
	 */
	@Override
	public int getBlinkPeriod() {
		return blinkPeriod;
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#updateMemoryAccessCycles(int)
	 */
	@Override
	protected void updateForMode() {
		switch (modeNumber) {
		case MODE_TEXT2:
			vdpMmio.setMemoryAccessCycles(1);
			break;
		case MODE_BITMAP:
		case MODE_GRAPHICS3:
			vdpMmio.setMemoryAccessCycles(2);
			break;
		case MODE_GRAPHICS4:
			rowstride = 256 / 2;
			pixperbyte = 2;
			pixshift = 4;
			pixmask = 0xf;
			
			vdpMmio.setMemoryAccessCycles(4);
			break;
		case MODE_GRAPHICS5:
			rowstride = 512 / 4;
			pixperbyte = 4;
			pixshift = 2;
			pixmask = 0x3;
			
			vdpMmio.setMemoryAccessCycles(4);
			break;
		case MODE_GRAPHICS6:
			rowstride = 512 / 2;
			pixperbyte = 2;
			pixshift = 4;
			pixmask = 0xf;
			
			vdpMmio.setMemoryAccessCycles(8);
			break;
		case MODE_GRAPHICS7:
			rowstride = 256;
			pixperbyte = 1;
			pixshift = 8;
			pixmask = 0xff;
			
			vdpMmio.setMemoryAccessCycles(8);
			break;
		default:
			super.updateForMode();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getVdpRegisterCount()
	 */
	@Override
	public int getVdpRegisterCount() {
		return 48;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.tms9918a.VdpTMS9918A#getGraphicsPageSize()
	 */
	@Override
	public int getGraphicsPageSize() {
		switch (getModeNumber()) {
		case MODE_GRAPHICS4:
		case MODE_GRAPHICS5:
			return 0x8000;
		case MODE_GRAPHICS6:
		case MODE_GRAPHICS7:
			return 0x10000;
		default:
			return super.getGraphicsPageSize();
		}
	}


    public void touchAbsoluteVdpMemory(int vdpaddr) {
    	vdpMemory.touchMemory(vdpaddr);
    }
    
	@Override
	public BitSet getRecordableRegs() {
		BitSet bs = new BitSet();
		// mode/memory/accel relevant ones
		bs.set(0, 20);
		
		// these affect memory and are handled directly
		bs.set(8, false);
		bs.set(14, false);
		bs.set(15, false);
		
		// accel regs
//		bs.set(20, 48);
		
		// and colors
		bs.set(REG_PAL0, REG_PAL0 + 16);
		
		// no status regs
		return bs;
	}
}
