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
public class TestVideoSpeedSwt24Bit extends TestVideoSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		return new SwtVideoRenderer(display, new ImageDataCanvas24Bit());
	}
}
