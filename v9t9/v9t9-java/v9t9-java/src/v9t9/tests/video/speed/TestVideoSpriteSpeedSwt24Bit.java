/**
 * 
 */
package v9t9.tests.video.speed;

import v9t9.emulator.clients.builtin.swt.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;


/**
 * @author ejs
 *
 */
public class TestVideoSpriteSpeedSwt24Bit extends TestVideoSpriteSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		SwtVideoRenderer swtVideoRenderer = new SwtVideoRenderer(null);
		swtVideoRenderer.setCanvas(new ImageDataCanvas24Bit());
		return swtVideoRenderer;
	}
}
