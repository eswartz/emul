/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
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

	public Sprite2RedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		vdpTouchBlock.sprite = modify_sprite_default;
		vdpTouchBlock.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSprite2Canvas(vdpCanvas, 8);
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		// sprite color table
		int sprcolbase = vdpModeInfo.sprite.base - 0x200;
		if (sprcolbase <= addr
				&& addr < vdpModeInfo.sprite.base) {
			
			vdpChanges.sprite |= (1<< ((addr - sprcolbase) >> 4));
			vdpchanged = 1;
			
			visible = true;
		}

		return super.touch(addr) || visible;
	}

}
