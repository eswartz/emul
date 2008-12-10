/**
 * 
 */
package v9t9.tests.video.speed;

import v9t9.emulator.clients.builtin.video.QtCanvas;
import v9t9.emulator.clients.builtin.video.QtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VideoRenderer;


/**
 * @author ejs
 *
 */
public class TestVideoSpeedQt24Bit extends TestVideoSpeedQtBase {
	@Override
	protected VideoRenderer createVideoRenderer() {
		return new QtVideoRenderer(new QtCanvas());
	}
}
