/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public class VdpV9938 extends VdpTMS9918A {

	public VdpV9938(MemoryDomain videoMemory, Vdp9938Mmio vdpMmio, VdpCanvas vdpCanvas) {
		super(videoMemory, vdpMmio, vdpCanvas);
	}

	protected byte[] allocVdpRegs() {
		return new byte[48];
	}
	
	@Override
	protected void establishVideoMode() {
		super.establishVideoMode();
	}
	
	@Override
	public byte readAbsoluteVdpMemory(int vdpaddr) {
		return ((Vdp9938Mmio) getVdpMmio()).readAbsoluteByte(vdpaddr);
	}
	
	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		return ((Vdp9938Mmio) getVdpMmio()).getByteReadMemoryAccess(addr); 
	}
	
	@Override
	public void touchAbsoluteVdpMemory(int vdpaddr, byte val) {
		if (vdpModeRedrawHandler != null) {
	    	vdpchanged |= vdpModeRedrawHandler.touch(vdpaddr);
	    	if (spriteRedrawHandler != null) {
	    		vdpchanged |= spriteRedrawHandler.touch(vdpaddr);
	    	}
		}
	}
	
}
