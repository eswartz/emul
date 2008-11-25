/**
 * 
 */
package v9t9.tests.video.speed;

import v9t9.emulator.clients.builtin.video.ImageDataCanvasPaletted;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;


/**
 * @author ejs
 *
 */
public class TestVideoSpriteSpeedSwtPaletted extends TestVideoSpriteSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		return new SwtVideoRenderer(display, new ImageDataCanvasPaletted());
	}
}
