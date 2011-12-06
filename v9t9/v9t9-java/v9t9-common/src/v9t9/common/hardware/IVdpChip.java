/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.common.hardware;


import v9t9.base.properties.IPersistable;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpModeInfo;

/** 
 * Handle the work of a VDP chip.  This maintains the memory,
 * register state, and  behavior of the VDP.
 * @author ejs
 */
public interface IVdpChip extends IPersistable {
	static public final SettingSchema settingDumpVdpAccess = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpVdpAccess", new Boolean(false));
	static public final SettingSchema settingVdpInterruptRate = new SettingSchema(
			ISettingsHandler.INSTANCE,
			"VdpInterruptRate", new Integer(60));
	// this should pretty much stay on
	static public final SettingSchema settingCpuSynchedVdpInterrupt = new SettingSchema(
			ISettingsHandler.INSTANCE,
			"CpuSynchedVdpInterrupt",
			new Boolean(true));

	public final static int VDP_INTERRUPT = 0x80;
	public final static int VDP_COINC = 0x40;
	public final static int VDP_FIVE_SPRITES = 0x20;
	public final static int VDP_FIFTH_SPRITE = 0x1f;
	final public static int R0_M3 = 0x2; // bitmap
	public final static int R0_EXTERNAL = 1;
	public final static int R1_RAMSIZE = 128;
	public final static int R1_NOBLANK = 64;
	public final static int R1_INT = 32;
	final public static int R1_M1 = 0x10; // text
	final public static int R1_M2 = 0x8; // multi
	public final static int R1_SPR4 = 2;
	public final static int R1_SPRMAG = 1;
	public final static int MODE_TEXT = 1;
	public final static int MODE_GRAPHICS = 0;
	public final static int MODE_BITMAP = 4;
	public final static int MODE_MULTI = 2;
	public final static int MODE_TEXT2 = 9;
	public final static int MODE_GRAPHICS3 = 8;
	public final static int MODE_GRAPHICS4 = 12;
	public final static int MODE_GRAPHICS5 = 16;
	public final static int MODE_GRAPHICS6 = 20;
	public final static int MODE_GRAPHICS7 = 28;
	/** Write a VDP register. 
    */
    void writeVdpReg(int reg, byte val);
    
    /** Read a VDP register. 
     */
    byte readVdpReg(int reg);
    
    /** Read VDP status.
     */
    byte readVdpStatus();

    /** Touch byte in the absolute VDP memory address
     * and note the side effects.
     */
    void touchAbsoluteVdpMemory(int vdpaddr);

    /** Read byte from absolute VDP memory at the given address
     */
    byte readAbsoluteVdpMemory(int vdpaddr);
    /** Write byte to absolute VDP memory at the given address,
     * also touching it.
     */
	void writeAbsoluteVdpMemory(int vdpaddr, byte byt);
    
    ByteMemoryAccess getByteReadMemoryAccess(int vdpaddr); 
    
    /** Update video canvas periodically */
    boolean update();

	//VdpMmio getVdpMmio();
	//void setVdpMmio(VdpMmio mmio);

	IMemoryDomain getVideoMemory();

	/** 60Hz timer.  Use this or syncVdpInterrupt / addCpuCycles */
	void tick();
	
	/** coprocessing */
	boolean isThrottled();
	void work();
	
	void setCanvas(IVdpCanvas canvas);
	IVdpCanvas getCanvas();

	/** This is called regularly from the CPU and should trigger the VDP
	 * interrupt according to the desired frequency. 
	 * @param machine */
	void syncVdpInterrupt(IMachine machine);

	/** Inform the VDP of the given number of cycles invoked on CPU side. */
	void addCpuCycles(int cycles);
	
	/** Get the handler for video-mode specific handling */
	//VdpModeRedrawHandler getVdpModeRedrawHandler();

	int getRegisterCount();
	String getRegisterName(int reg);
	String getRegisterTooltip(int reg);
	byte getRegister(int reg);
	void setRegister(int reg, byte value);

	VdpModeInfo getModeInfo();
	
	/**
	 * Tell whether interlacing is active.
	 * 
	 * For use in rendering, we need to know whether raw R9_IL (interlace) bit is set
	 * and also the R9_EO (even/odd) bit is set, which would provide the page flipping
	 * required to *see* two pages.  Finally, the "odd" graphics page must be visible
	 * for the flipping and interlacing to occur.
	 * @return
	 */
	
	public boolean isInterlacedEvenOdd();

	/**
	 * @return
	 */
	int getGraphicsPageSize();

	/**
	 * @return
	 */
	int getModeNumber();

}
