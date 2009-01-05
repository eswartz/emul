/**
 * 
 */
package v9t9.tests.video.speed;

import v9t9.emulator.clients.builtin.swt.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.ImageDataCanvasPaletted;


/**
 * @author ejs
 *
 */
public class TestVideoSpriteSpeedSwtPaletted extends TestVideoSpriteSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		SwtVideoRenderer swtVideoRenderer = new SwtVideoRenderer();
		swtVideoRenderer.setCanvas(new ImageDataCanvasPaletted());
		return swtVideoRenderer;

	}
}
