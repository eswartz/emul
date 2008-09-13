/**
 * 
 */
package v9t9.tests;

import v9t9.emulator.clients.builtin.video.ImageDataCanvasPaletted;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;


/**
 * @author ejs
 *
 */
public class TestVideoSpeedSwtPaletted extends TestVideoSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		return new SwtVideoRenderer(display, new ImageDataCanvasPaletted());
	}
}
