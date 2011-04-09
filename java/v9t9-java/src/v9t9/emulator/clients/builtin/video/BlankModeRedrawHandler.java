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
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);
		
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

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData() {
		
	}
}
