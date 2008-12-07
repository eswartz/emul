/**
 * 
 */
package v9t9.tests.video.speed;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;


/**
 * @author ejs
 *
 */
public class TestVideoSpriteSpeedSwt24Bit extends TestVideoSpriteSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		SwtVideoRenderer swtVideoRenderer = new SwtVideoRenderer(display);
		swtVideoRenderer.setCanvas(new ImageDataCanvas24Bit());
		return swtVideoRenderer;
	}
}
