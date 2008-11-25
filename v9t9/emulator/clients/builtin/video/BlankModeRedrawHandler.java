/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.VdpHandler;


/**
 * @author ejs
 *
 */
public class BlankModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	public BlankModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);
		vdpModeInfo.screen.base = 0;
		vdpModeInfo.screen.size = 0;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = 0;
		vdpModeInfo.patt.size = 0;
		vdpModeInfo.sprite.base = 0;
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = 0;
		vdpModeInfo.sprpat.size = 0;
		vdpCanvas.setSize(256, 192);
		vdpTouchBlock.patt = null;
		vdpTouchBlock.sprite = vdpTouchBlock.sprpat = null;
		vdpTouchBlock.screen = null;
		vdpTouchBlock.color = null;


	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void propagateTouches() {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*if (force) {
			for (int i = 0; i < 768; i++) {
				blocks[i].r = (i / 32) * 8;
				blocks[i].c = (i % 32) * 8;
			}
			return 768;
		}*/
		return 0;
	}

}
