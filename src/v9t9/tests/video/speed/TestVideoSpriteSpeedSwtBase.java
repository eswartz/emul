/**
 * 
 */
package v9t9.tests.video.speed;

import org.eclipse.swt.widgets.Display;


/**
 * @author ejs
 *
 */
public abstract class TestVideoSpriteSpeedSwtBase extends TestVideoSpriteSpeedBase {

	protected Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = new Display();
		videoRenderer = createVideoRenderer();
		canvas = videoRenderer.getCanvas();
	}

	@Override
	protected void tearDown() throws Exception {
		display.dispose();
	}
	
	@Override
	protected void handleEvents() {
		while (display.readAndDispatch()) /**/ ;
	}
}
