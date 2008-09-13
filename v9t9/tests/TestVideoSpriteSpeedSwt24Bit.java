/**
 * 
 */
package v9t9.tests;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;


/**
 * @author ejs
 *
 */
public class TestVideoSpriteSpeedSwt24Bit extends TestVideoSpriteSpeedSwtBase {
	@Override
	protected SwtVideoRenderer createVideoRenderer() {
		return new SwtVideoRenderer(display, new ImageDataCanvas24Bit());
	}
}
