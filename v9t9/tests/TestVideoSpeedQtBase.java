/**
 * 
 */
package v9t9.tests;

import com.trolltech.qt.gui.QApplication;


/**
 * @author ejs
 *
 */
public abstract class TestVideoSpeedQtBase extends TestVideoSpeedBase {

	static boolean inited = false;
	
	@Override
	protected void setUp() throws Exception {
		if (!inited) {
			QApplication.initialize(new String[] {"foo"});
			inited = true;
		}
		videoRenderer = createVideoRenderer();
		videoRenderer.setBlank(false);
		videoRenderer.resize(256, 192);
		canvas = videoRenderer.getCanvas();
	}

	@Override
	protected void tearDown() throws Exception {
	}
	
	@Override
	protected void handleEvents() {
		while (QApplication.hasPendingEvents())
			QApplication.processEvents();
	}
}
