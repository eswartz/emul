/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.vdp;

/** Render VDP video
 * @author ejs
 */
public interface Handler {
    /** Write a VDP register. 
    */
    public abstract void writeVdpReg(byte reg, byte val, byte old);
    
    /** Read VDP status.
     */
    public abstract byte readVdpStatus();

    /** Write byte to VDP memory.
     */
    public abstract void writeVdpMemory(short vdpaddr, byte val);
}
