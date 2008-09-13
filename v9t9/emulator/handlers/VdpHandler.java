/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.handlers;

/** 
 * Render VDP video.  This is not responsible for managing memory,
 * but for updating the visual appearance of the screen in response
 * to VDP memory / register changes detected by the memory subsystem. 
 * @author ejs
 */
public interface VdpHandler {
    /** Write a VDP register. 
    */
    void writeVdpReg(byte reg, byte val);
    
    /** Read VDP status.
     */
    byte readVdpStatus();

    /** Write byte to VDP memory.  Issued only when a change is detected.
     */
    void writeVdpMemory(short vdpaddr, byte val);

    /** Update video periodically */
    void update();
}
