/*
  VdpTMS9918A.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.video.tms9918a;

import static v9t9.common.hardware.VdpTMS9918AConsts.*;

import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.actors.VdpDataDemoActor;
import v9t9.engine.demos.actors.VdpRegisterDemoActor;
import v9t9.engine.hardware.BaseCruChip;
import v9t9.engine.memory.VdpMmio;

/**
 * This is the 99/4A VDP chip.
 * <p>
 * Mode bits:
 * <p>
 * R0:	M3 @ 1
 * R1:	M1 @ 4, M2 @ 3 
 * <p>
 * <pre>
 *                   M1  M2  M3  
 * Text 1 mode:      1   0   0   = 1
 * Multicolor:       0   1   0   = 2
 * Graphics 1 mode:  0   0   0   = 0
 * Graphics 2 mode:  0   0   1   = 4
 * </pre>
 * @author ejs
 */
public class VdpTMS9918A implements IVdpChip, IVdpTMS9918A {
	private final static Map<Integer, String> regNames = new HashMap<Integer, String>();
	private final static Map<String, Integer> regIds = new HashMap<String, Integer>();
	
	private static void register(int reg, String id) {
		regNames.put(reg, id);
		regIds.put(id, reg);
	}
	
	static {
		for (int i = 0; i < 8; i++) {
			register(i, "VR" + i);
		}
		register(REG_ST, "ST");
		register(REG_SCANLINE, "SCAN");
	}
	
	
	protected IMemoryDomain vdpMemory;

	protected byte vdpregs[];

	protected byte vdpStatus;
	
	protected VdpMmio vdpMmio;

	/** The circular counter for VDP interrupt timing. */
	private int vdpInterruptFrac;
	/** The number of CPU cycles corresponding to 1/60 second */
	private int vdpInterruptLimit;
	private int vdpInterruptDelta;
	
	/** The circular counter for VDP scanline timing. */
	private int vdpScanlineFrac;
	/** The number of CPU cycles corresponding to one scanline */
	private int vdpScanlineLimit;

	private int throttleCount;
	protected final IMachine machine;
	private int fixedTimeVdpInterruptDelta;
	private IProperty cyclesPerSecond;
	protected IProperty vdpInterruptRate;
	private IProperty realTime;
	private IProperty cpuSynchedVdpInterrupt;
	private boolean isCpuSynchedVdpInterrupt;
	protected IProperty dumpVdpAccess;
	protected IProperty dumpFullInstructions;
	private IProperty throttleInterrupts;

	protected ListenerList<IRegisterWriteListener> listeners = new ListenerList<IRegisterWriteListener>();

	protected int modeNumber;

	private int vdpScanline;

	protected int width;

	protected int scanlineCount;
	private int scanline;
	
	public VdpTMS9918A(IMachine machine) {
		this.machine = machine;
		
		ISettingsHandler settings = Settings.getSettings(machine);
		
		cyclesPerSecond = settings.get(ICpu.settingCyclesPerSecond);
		vdpInterruptRate = settings.get(settingVdpInterruptRate);
		realTime = settings.get(ICpu.settingRealTime);
		cpuSynchedVdpInterrupt = settings.get(settingCpuSynchedVdpInterrupt);
		isCpuSynchedVdpInterrupt = cpuSynchedVdpInterrupt.getBoolean();
		throttleInterrupts = settings.get(IMachine.settingThrottleInterrupts);
		dumpFullInstructions = settings.get(ICpu.settingDumpFullInstructions);
		dumpVdpAccess = settings.get(settingDumpVdpAccess);
		
		vdpStatus = (byte) VDP_INTERRUPT;
		
		vdpInterruptRate.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				recalcInterruptTiming();
			}
			
		});
		
		cyclesPerSecond.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				recalcInterruptTiming();
			}
			
		});
		
		realTime.addListener(new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				cpuSynchedVdpInterrupt.setBoolean(setting.getBoolean());
				isCpuSynchedVdpInterrupt = cpuSynchedVdpInterrupt.getBoolean();
			}
		});
		
		this.vdpMemory = machine.getMemory().getDomain(IMemoryDomain.NAME_VIDEO);
		this.vdpregs = allocVdpRegs();
		
		initRegisters();
		
		machine.getDemoManager().registerActorProvider(new VdpDataDemoActor.Provider());
		machine.getDemoManager().registerActorProvider(new VdpRegisterDemoActor.Provider());
	}

	public void initRegisters() {
		// zeroes are fine
	}

	protected void recalcInterruptTiming() {
		if (vdpInterruptRate.getInt() > 0)
			vdpInterruptLimit = cyclesPerSecond.getInt() / vdpInterruptRate.getInt();
		else
			vdpInterruptLimit = Integer.MAX_VALUE;
        vdpInterruptFrac = 0;
        
        vdpScanlineLimit = vdpInterruptLimit / 192;
        vdpScanlineFrac = 0;
        if (scanlineCount > 0) {
        	vdpScanlineLimit = vdpInterruptLimit / scanlineCount;
        }
        
        fixedTimeVdpInterruptDelta = (int) ((long) vdpInterruptRate.getInt() * 65536 / machine.getTicksPerSec());
        //System.out.println("VDP interrupt target: " + Cpu.settingCyclesPerSecond.getInt() + " /  " + settingVdpInterruptRate.getInt() + " = " + vdpInterruptLimit);		
	}

	public void log(String msg) {
		if (dumpVdpAccess.getBoolean()) {
			PrintWriter pw = Logging.getLog(dumpFullInstructions);
			if (pw != null)
				pw.println("[VDP] " + msg);
		}
	}
	
	public IMemoryDomain getVideoMemory() {
		return vdpMemory;
	}
	 
	public void setVdpMmio(VdpMmio vdpMmio) {
		this.vdpMmio = vdpMmio;
	}

	public VdpMmio getVdpMmio() {
		return vdpMmio;
	}
	protected byte[] allocVdpRegs() {
		return new byte[8];
	}
	
	/**
	 * @param reg
	 * @param value new value
	 */
	protected void fireRegisterChanged(final int reg, final int value) {
		if (!listeners.isEmpty()) {
			for (Object listener : listeners.toArray()) {
				try {
					((IRegisterWriteListener)listener).registerChanged(reg, value);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
    	}
	}

	/* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#readVdpStatus()
     */
    public byte readVdpStatus() {
		/* >8802, status read and acknowledge interrupt */
    	byte ret = vdpStatus;
    	setRegister(REG_ST, vdpStatus & ~VDP_INTERRUPT);
    	machine.getCru().acknowledgeInterrupt(VDP_INTERRUPT);

        return ret;
    }

    public void touchAbsoluteVdpMemory(int vdpaddr) {
    	vdpMemory.touchMemory(vdpaddr & getModeAddressMask());
    }
    
    public byte readAbsoluteVdpMemory(int vdpaddr) {
    	return vdpMmio.readFlatMemory(vdpaddr);
    }
    
    public void writeAbsoluteVdpMemory(int vdpaddr, byte byt) {
    	vdpMmio.writeFlatMemory(vdpaddr, byt);
    }

	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		return vdpMmio.getByteReadMemoryAccess(addr);
	}
	
	public void tick() {
		 if (isCpuSynchedVdpInterrupt)
			 return;
		 
		 // in this model, we use the system clock to ensure reliable VDP
		 // interrupts, because the CPU speed is unbounded and unreliable.
		 
		if (machine.isExecuting()) {
			vdpInterruptDelta += fixedTimeVdpInterruptDelta;
			//System.out.print("[VDP delt:" + vdpInterruptDelta + "]");
			if (vdpInterruptDelta >= 65536) {
		
				vdpInterruptDelta -= 65536;
				
				for (int row = 0; row < scanlineCount; row++) {
					onScanline(row);
				}
				
				doTick();
			}
		}
		
	}
	

	public void syncVdpInterrupt(IMachine machine) {
		if (!isCpuSynchedVdpInterrupt)
			return;

		// in this model, the CPU is running at a fixed rate,
		// so we can trigger VDP interrupts in lockstep
		// with the CPU.
		
		if (vdpInterruptFrac < 0)
			vdpInterruptFrac = 0;

		if (vdpScanlineFrac < 0)
			vdpScanlineFrac = 0;
		
		while (vdpScanlineFrac >= vdpScanlineLimit) {
			vdpScanlineFrac -= vdpScanlineLimit;
			
			if (vdpScanline >= scanlineCount)
				vdpScanline -= scanlineCount;
			
			onScanline(vdpScanline);
			
			vdpScanline++;
		}
		
		if (vdpInterruptFrac >= vdpInterruptLimit) {
			vdpInterruptFrac -= vdpInterruptLimit;
			
			doTick();
		}
	}
	
	/**
	 */
	protected void onScanline(int vdpScanline) {
		fireRegisterChanged(REG_SCANLINE, vdpScanline);
	}

	/**
	 * 
	 */
	protected void doTick() {
		if (true /*|| machine.getExecutor().nVdpInterrupts < settingVdpInterruptRate.getInt()*/) {
    		if (throttleInterrupts.getBoolean()) {
    			if (throttleCount-- < 0) {
    				throttleCount = 6;
    			} else {
    				return;
    			}
    		}
    		
    		// a real interrupt only occurs if wanted, but always marked
    		setRegister(REG_ST, vdpStatus | VDP_INTERRUPT);
    		if ((vdpregs[1] & R1_INT) != 0) {
    			triggerInterrupt();
    		}
		}
		//System.out.print('!');
		
	}
	
	/**
	 * 
	 */
	protected void triggerInterrupt() {
		machine.getExecutor().vdpInterrupt();
		
		ICruChip cru = machine.getCru();
		if (cru instanceof BaseCruChip) {
			cru.triggerInterrupt(((BaseCruChip) cru).intVdp);
		}
				
	}

	public boolean isThrottled() {
		return true;
	}
	
	public void work() {
		
	}

	public void saveState(ISettingSection section) {
		String[] regState = new String[vdpregs.length];
		for (int i = 0; i < vdpregs.length; i++) {
			regState[i] = HexUtils.toHex2(vdpregs[i]);
		}
		section.put("Registers", regState);
		//settingDumpVdpAccess.saveState(section);
		cpuSynchedVdpInterrupt.saveState(section);
		vdpInterruptRate.saveState(section);
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		
		String[] regState = section.getArray("Registers");
		if (regState != null) {
			for (int i = 0; i < regState.length; i++) {
				byte val = (byte) Integer.parseInt(regState[i], 16);
				loadVdpReg(i, val);
			}
		}
		
		//settingDumpVdpAccess.loadState(section);
		cpuSynchedVdpInterrupt.loadState(section);
		isCpuSynchedVdpInterrupt = cpuSynchedVdpInterrupt.getBoolean();

		vdpInterruptRate.loadState(section);
	}
	
	
	protected void loadVdpReg(int num, byte val) {
		setRegister(num, val);
	}

	
	public void addCpuCycles(int cycles) {
		vdpInterruptFrac += cycles;
		vdpScanlineFrac += cycles;
	}
	
	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		listeners.remove(listener);
	}
	
    public String getGroupName() {
    	return "VDP TMS9918A Registers";
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.machine.IRegisterAccess#getFirstRegister()
     */
    @Override
    public int getFirstRegister() {
    	return REG_SCANLINE;
    }
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return 8 - getFirstRegister();
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpChip#getRecordableRegs()
	 */
	@Override
	public BitSet getRecordableRegs() {
		BitSet bs = new BitSet();
		int first = getFirstRegister();
		bs.set(REG_SCANLINE - first);
		bs.set(0 - first, 8);
		return bs;
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == REG_SCANLINE) {
			return scanline;
		} else if (reg == REG_ST) {
			return vdpStatus;
		} else if (reg < vdpregs.length) {
			return vdpregs[reg] & 0xff;
		} else {
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterInfo(int)
	 */
	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		String id = getRegisterId(reg);
		if (id == null)
			return null;
		return new RegisterInfo(id, 
				getRegisterFlags(reg),
				getRegisterSize(reg),
				getRegisterName(reg));
				
	}
	
	/**
	 * @param reg
	 * @return
	 */
	protected int getRegisterSize(int reg) {
		return 1;
	}

	protected int getRegisterFlags(int reg) {
		return IRegisterAccess.FLAG_ROLE_GENERAL +
			(reg == REG_ST ? IRegisterAccess.FLAG_VOLATILE : 0);
	}


	protected String getRegisterId(int reg) {
		return regNames.get(reg);
	}

	@Override
	public int getRegisterNumber(String id) {
		Integer num = regIds.get(id);
		return num != null ? num : Integer.MIN_VALUE;
	}
	
	protected String getRegisterName(int reg) {
		switch (reg) {
		case REG_ST:
			return "Status";
		}
		switch (reg) {
		case 0:
			return "Mode Reg 0";
		case 1:
			return "Mode Reg 1";
		case 2: 
			return "Screen Offset";
		case 3: 
			return "Color Table";
		case 4: 
			return "Pattern Table";
		case 5: 
			return "Sprite Table";
		case 6: 
			return "Sprite Patterns";
		case 7: 
			return "Backdrop/Text Colors";
		}
		return null;
	}
	

	protected String yOrN(String label, int i) {
		return i != 0 ? label : "";
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		switch (reg) {
		case REG_ST:
			return getStatusString(vdpStatus);
		}
		byte val = vdpregs[reg];
		switch (reg) {
		case 0:
			return caten(yOrN("Bitmap", val & 0x2), yOrN("Ext Vid", val & 0x1))
				+ " (" + getModeName() + ")";
		case 1:
			return caten(yOrN("16K", val & 0x80), yOrN("Blank", val & 0x40),
					yOrN("Int on", val & 0x20), yOrN("Multi", val & 0x10),
					yOrN("Text", val & 0x08),
					yOrN("Size 4", val & 0x02), yOrN("Mag", val & 0x01))
				+ " (" + getModeName() + ")";
		case 2: 
			return "Screen: " + HexUtils.toHex4(getScreenTableBase());
		case 3: 
			return "Colors: " + HexUtils.toHex4(getColorTableBase())
			+ (isBitmapMode() ?
					" | Mask: " + HexUtils.toHex4(getBitmapModeColorMask()) 
							: "");
		case 4: 
			return "Patterns: " + HexUtils.toHex4(getPatternTableBase())
			+ (isBitmapMode() ?
					" | Mask: " + HexUtils.toHex4(getBitmapModePatternMask()) 
					: "");
		case 5: 
			return "Sprites: " + HexUtils.toHex4(getSpriteTableBase());
		case 6: 
			return "Sprite patterns: " + HexUtils.toHex4(getSpritePatternTableBase());
		case 7: 
			return "Color BG: " + HexUtils.toHex2(val & 0x7) 
			+ " | FG: " + HexUtils.toHex2((val & 0xf0) >> 4);
		}
		return null;
	}
	
	protected String getStatusString(byte s) {
		return caten(yOrN("Int", s & 0x80),
				yOrN("5 Sprites", s & 0x40),
				yOrN("Coinc", s & 0x20))
				+ " | 5th: " + (s & 0x1f);
	}

	/**
	 * @param yOrN
	 * @param yOrN2
	 * @param yOrN3
	 * @return
	 */
	protected String caten(String... vals) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String v : vals) {
			if (first)
				first = false;
			else
				sb.append(" | ");
			sb.append(v.length() == 0 ? "0" : v);
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#setRegister(int, byte)
	 */
	@Override
	public int setRegister(int reg, int value) {
		int old;
		if (reg == REG_SCANLINE) {
			old = scanline;
			scanline = value;
		} else if (reg == REG_ST) {
			old = vdpStatus & 0xff;
			vdpStatus = (byte) value;
		} else {
			if (reg >= vdpregs.length)
				return 0;
			
			old = vdpregs[reg] & 0xff;
			vdpregs[reg] = (byte) value;
			
			doSetVdpReg(reg, (byte) old, (byte) value);
    		
			modeNumber = calculateModeNumber();
    		updateForMode();
		}
		
		if (dumpFullInstructions.getBoolean() && dumpVdpAccess.getBoolean())
			log("register " + (reg < 0 ? "ST" : ""+reg) + " " + HexUtils.toHex2(old) + " -> " + HexUtils.toHex2(value));
		
		fireRegisterChanged(reg, value);
		return old;
	}

	/**
	 * @param reg
	 * @param b
	 * @param val
	 */
	protected void doSetVdpReg(int reg, byte old, byte val) {
//		/* if interrupts enabled, and interrupt was pending, trigger it */
//		if ((val & R1_INT) != 0 
//		&& 	(old & R1_INT) == 0) 
//		{
//			triggerInterrupt();
//		}

	}

	/**
	 * @param modeNumber
	 */
	protected void updateForMode() {
		setSize(256, 192);
		
		if ((vdpregs[1] & R1_NOBLANK) == 0) {
			vdpMmio.setMemoryAccessCycles(0);
			return;
		}

		switch (modeNumber) {
		case MODE_GRAPHICS:
			vdpMmio.setMemoryAccessCycles(8);
			break;
		case MODE_MULTI:
			vdpMmio.setMemoryAccessCycles(2);
			break;
		case MODE_TEXT:
			vdpMmio.setMemoryAccessCycles(1);
			break;
		case MODE_BITMAP:
			vdpMmio.setMemoryAccessCycles(8);
			break;
		}
	}

	/**
	 * @param width
	 * @param height
	 */
	protected void setSize(int width, int height) {
		this.width = width;
		this.scanlineCount = height;
	}

	/**
	 * @return
	 */
	public String getModeName() {
		switch (getModeNumber()) {
		case MODE_BITMAP: return "Bitmap";
		case MODE_GRAPHICS: return "Graphics";
		case MODE_MULTI: return "MultiColor";
		case MODE_TEXT: return "Text";
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpChip#isInterlacedEvenOdd()
	 */
	@Override
	public boolean isInterlacedEvenOdd() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpChip#getGraphicsPageSize()
	 */
	@Override
	public int getGraphicsPageSize() {
		return 0;
	}
	

    /**
     * Get the address a table will take given the mode and memory size
     * @return
     */
    protected int getModeAddressMask() {
    	return vdpMmio.getMemorySize() - 1;
    }
    

    @Override
	public int getScreenTableBase() {
		return (vdpregs[2] * 0x400) & getModeAddressMask();
	}

    /* (non-Javadoc)
     * @see v9t9.common.hardware.IVdpTMS9918A#getScreenTableSize()
     */
    @Override
    public int getScreenTableSize() {
    	return getModeNumber() == MODE_TEXT ? 960 : 768;
    }
    
    @Override
	public int getSpritePatternTableBase() {
		return (vdpregs[6] * 0x800) & getModeAddressMask();
	}

    /* (non-Javadoc)
     * @see v9t9.common.hardware.IVdpTMS9918A#getSpritePatternTableSize()
     */
    @Override
    public int getSpritePatternTableSize() {
    	return 2048;
    }
	
	@Override
	public int getSpriteTableBase() {
		return (vdpregs[5] * 0x80) & getModeAddressMask();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#getSpriteTableSize()
	 */
	@Override
	public int getSpriteTableSize() {
		return 128;
	}

	protected boolean isBitmapMode() {
		return modeNumber == MODE_BITMAP;
	}
	
	@Override
	public int getPatternTableBase() {
		if (isBitmapMode())
			return (vdpregs[4] & 0x04) * 0x800;
		else
			return ((vdpregs[4] & 0xff) * 0x800) & getModeAddressMask();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#getPatternTableSize()
	 */
	@Override
	public int getPatternTableSize() {
		return isBitmapMode() ? 0x1800 : 0x800;
	}

	@Override
	public int getColorTableBase() {
		return isBitmapMode() ? (vdpregs[3] & 0x80) * 0x40
				: ((vdpregs[3] & 0xff) * 0x40) & getModeAddressMask();
	}

	@Override
	public int getColorTableSize() {
		return isBitmapMode() ? 0x1800 : 32;
	}
	
	final public int getModeNumber() {
		return modeNumber;
	}
	protected int calculateModeNumber() {
		int reg0 = vdpregs[0] & R0_M3;
		int reg1 = vdpregs[1] & R1_M1 + R1_M2;
    	
    	if (reg0 == R0_M3) {
    		// can support multi+bitmap or text+bitmap modes too... but not now
    		return MODE_BITMAP;
    	}
    	if (reg1 == R1_M2)
    		return MODE_MULTI;
    	if (reg1 == R1_M1)
    		return MODE_TEXT;
    	return MODE_GRAPHICS;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#getBitmapModeColorMask()
	 */
	@Override
	public int getBitmapModeColorMask() {
		return (short) (vdpregs[3] & 0x7f) << 6 | 0x3f;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#getBitmapModePatternMask()
	 */
	@Override
	public int getBitmapModePatternMask() {
		// thanks, Thierry!
		// in "bitmap text" mode, the full pattern table is always addressed,
		// otherwise, the color bits are used in the pattern masking
		if ((vdpregs[1] & 0x10) != 0)
			return (vdpregs[4] & 0x03) << 11 | 0x7ff;
		else
			return (vdpregs[4] & 0x03) << 11 | getBitmapModeColorMask() & 0x7ff;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#isBitmapMonoMode()
	 */
	@Override
	public boolean isBitmapMonoMode() {
		boolean isMono = isBitmapMode() && getBitmapModeColorMask() != 0x1fff;
		return isMono;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpTMS9918A#getVdpRegisterCount()
	 */
	@Override
	public int getVdpRegisterCount() {
		return 8;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpChip#getMemorySize()
	 */
	@Override
	public int getMemorySize() {
		return vdpMmio.getMemorySize();
	}
	

    /** Tell if the registers indicate a blank screen. */
    public boolean isBlank() {
    	return (vdpregs[1] & R1_NOBLANK) == 0;
    }
    
	@Override
	public BitSet getVisibleMemory(int granularityShift) {
		BitSet bs = new BitSet();
		if (isBlank())
			return bs;
		
		populateBits(bs, granularityShift,
				getScreenTableBase(),
				getScreenTableSize());
		populateBits(bs, granularityShift,
				getPatternTableBase(),
				getPatternTableSize());
		populateBits(bs, granularityShift,
				getColorTableBase(),
				getColorTableSize());
		populateBits(bs, granularityShift,
				getSpriteTableBase(),
				getSpriteTableSize());
		populateBits(bs, granularityShift,
				getSpritePatternTableBase(),
				getSpritePatternTableSize());
		return bs;
	}

	private void populateBits(BitSet bs, int granularityShift,
			int base, int size) {
		int round = ~0 >>> (32 - granularityShift);
		bs.set(base >>> granularityShift, 
			(base + size + round) >>> granularityShift);
	}
}
