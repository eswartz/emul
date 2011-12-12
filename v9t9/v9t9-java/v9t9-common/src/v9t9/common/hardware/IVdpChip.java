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
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.SettingSchema;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpModeInfo;

/** 
 * Handle the work of a VDP chip.  This maintains the memory,
 * register state, and  behavior of the VDP.
 * @author ejs
 */
public interface IVdpChip extends IPersistable, IRegisterAccess {
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

	int VDP_INTERRUPT = 0x80;
	int VDP_COINC = 0x40;
	int VDP_FIVE_SPRITES = 0x20;
	int VDP_FIFTH_SPRITE = 0x1f;
	int R0_M3 = 0x2; // bitmap
	int R0_EXTERNAL = 1;
	int R1_RAMSIZE = 128;
	int R1_NOBLANK = 64;
	int R1_INT = 32;
	int R1_M1 = 0x10; // text
	int R1_M2 = 0x8; // multi
	int R1_SPR4 = 2;
	int R1_SPRMAG = 1;
	int MODE_TEXT = 1;
	int MODE_GRAPHICS = 0;
	int MODE_BITMAP = 4;
	int MODE_MULTI = 2;
	int MODE_TEXT2 = 9;
	int MODE_GRAPHICS3 = 8;
	int MODE_GRAPHICS4 = 12;
	int MODE_GRAPHICS5 = 16;
	int MODE_GRAPHICS6 = 20;
	int MODE_GRAPHICS7 = 28;
	
	int REG_ST = -1;
	int REG_SR0 = 48;
	int REG_PAL0 = 48 + 9;
	
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
	
	/** This is called regularly from the CPU and should trigger the VDP
	 * interrupt according to the desired frequency. 
	 * @param machine */
	void syncVdpInterrupt(IMachine machine);

	/** Inform the VDP of the given number of cycles invoked on CPU side. */
	void addCpuCycles(int cycles);
	
	interface IVdpListener {
		/** Report that a VDP register changed */
		void vdpRegisterChanged(int reg);
		/** Report that a palette register changed
		 * @param color the affected color  */
		void paletteColorChanged(int color);
	}
	
	void addListener(IVdpListener listener);
	void removeListener(IVdpListener listener);

	@Deprecated
	void setCanvas(IVdpCanvas canvas);
	@Deprecated
	IVdpCanvas getCanvas();

	@Deprecated
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
	
	@Deprecated
	public boolean isInterlacedEvenOdd();

	/**
	 * @return
	 */
	@Deprecated
	int getGraphicsPageSize();

	/**
	 * @return
	 */
	@Deprecated
	int getModeNumber();

}
