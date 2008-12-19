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
public class TestVideoSpeedSwtPaletted extends TestVideoSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		SwtVideoRenderer swtVideoRenderer = new SwtVideoRenderer();
		swtVideoRenderer.setCanvas(new ImageDataCanvasPaletted());
		return swtVideoRenderer;

	}
}
