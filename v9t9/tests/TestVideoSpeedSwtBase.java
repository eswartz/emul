/**
 * 
 */
package v9t9.tests;

import org.eclipse.swt.widgets.Display;


/**
 * @author ejs
 *
 */
public abstract class TestVideoSpeedSwtBase extends TestVideoSpeedBase {

	protected Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = new Display();
		videoRenderer = createVideoRenderer();
		videoRenderer.setBlank(false);
		videoRenderer.resize(256, 192);
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
