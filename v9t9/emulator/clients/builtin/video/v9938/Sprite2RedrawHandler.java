/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpSpriteCanvas;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.emulator.clients.builtin.video.tms9918a.SpriteRedrawHandler;
import v9t9.engine.VdpHandler;

/**
 * Sprite mode 2
 * <p>
 * -- Separate color table -- 512 bytes before sprite attribute table
 * -- Color in sprite is ignored
 * -- 16 colors per sprite, one per line
 * -- EC | CC | IC | 0 | color
 * -- early clock, priority enable, collision detect
 * @author ejs
 * 
 */
public class Sprite2RedrawHandler extends SpriteRedrawHandler {

	protected VdpTouchHandler modify_sprite2_default = new VdpTouchHandler() {

		public void modify(int offs) {
			if (offs < 0)
				 /* color table */;
			vdpChanges.sprite |= (1<<(offs >> 2));
			vdpchanged = 1;
		}

	};

	public Sprite2RedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		vdpTouchBlock.sprite = modify_sprite2_default;
		vdpTouchBlock.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSpriteCanvas(vdpCanvas, 8);
	}

}
