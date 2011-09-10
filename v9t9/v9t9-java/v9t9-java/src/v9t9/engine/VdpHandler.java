/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine;

import org.ejs.coffee.core.properties.IPersistable;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

/** 
 * Render VDP video.  This is not responsible for managing memory,
 * but for updating the visual appearance of the screen in response
 * to VDP memory / register changes detected by the memory subsystem. 
 * @author ejs
 */
public interface VdpHandler extends IPersistable{
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
     * @param val the value written -- note, this is not the same as writing the memory,
     * which should mirror this result (historical reasons -- DemoClient has no memory)
     */
    void touchAbsoluteVdpMemory(int vdpaddr, byte val);

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

	VdpMmio getVdpMmio();
	void setVdpMmio(VdpMmio mmio);

	MemoryDomain getVideoMemory();

	/** 60Hz timer.  Use this or syncVdpInterrupt / addCpuCycles */
	void tick();
	
	/** coprocessing */
	boolean isThrottled();
	void work();
	
	void setCanvas(VdpCanvas canvas);
	VdpCanvas getCanvas();

	/** This is called regularly from the CPU and should trigger the VDP
	 * interrupt according to the desired frequency. 
	 * @param machine */
	void syncVdpInterrupt(Machine machine);

	/** Inform the VDP of the given number of cycles invoked on CPU side. */
	void addCpuCycles(int cycles);
	
	/** Get the handler for video-mode specific handling */
	VdpModeRedrawHandler getVdpModeRedrawHandler();

	int getRegisterCount();
	String getRegisterName(int reg);
	String getRegisterTooltip(int reg);
	byte getRegister(int reg);
	void setRegister(int reg, byte value);

}
