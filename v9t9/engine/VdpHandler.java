/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

/** 
 * Render VDP video.  This is not responsible for managing memory,
 * but for updating the visual appearance of the screen in response
 * to VDP memory / register changes detected by the memory subsystem. 
 * @author ejs
 */
public interface VdpHandler {
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
    
    ByteMemoryAccess getByteReadMemoryAccess(int vdpaddr); 
    
    /** Update video canvas periodically */
    void update();

	VdpCanvas getCanvas();
	
	VdpMmio getVdpMmio();
	void setVdpMmio(VdpMmio mmio);

	MemoryDomain getVideoMemory();

	/** 60Hz timer */
	void tick();
}
